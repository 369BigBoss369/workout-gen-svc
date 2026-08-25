package com.workoutgensvc.core;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@Service
public class AIProcessManager {
    @Value("${chat2api.path}")
    private String chat2apiPath;

    @Value("${chat2api.command.start}")
    private String chat2apiCommand;

    @Value("${chat2api.command.setup}")
    private String chat2apiSetupCommand;

    @Value("${chat2api.access-token}")
    private String chat2apiAccessToken;

    private Process chat2apiProcess;

    @PostConstruct
    public void startChat2Api() throws IOException {
        Path chat2apiDir = Paths.get(chat2apiPath);

        if (!Files.exists(chat2apiDir)) {
            setup();
        }

        Path startScript = Paths.get(resolveScriptPath(chat2apiCommand));
        if (!Files.exists(startScript)) {
            log.warn("Chat2API start script not found. Looked for: {} (resolved to absolute path: {}). " +
                            "Working directory is: {}. Check that chat2api.command.start in application.properties " +
                            "matches an actual file relative to that working directory (without the .bat/.sh extension).",
                    startScript, startScript.toAbsolutePath(), System.getProperty("user.dir"));
            return;
        }

        try {
            startProcess();
            Thread.sleep(10000);
        } catch (Exception e) {
            log.error("Failed to start Chat2API: {}", e.getMessage());
        }

        log.info("Chat2Api initialization completed.");
    }

    private void startProcess() throws IOException {
        ProcessBuilder processBuilder = new ProcessBuilder();
        String script = resolveScriptPath(chat2apiCommand);
        if (isWindows()) {
            processBuilder.command("cmd", "/c", "start", script);
        } else {
            processBuilder.command("sh", "-c", "./" + script);
        }

        if (chat2apiAccessToken != null && !chat2apiAccessToken.trim().isEmpty()) {
            processBuilder.environment().put("CHAT2API_ACCESS_TOKEN", chat2apiAccessToken);
        }

        chat2apiProcess = processBuilder.start();
    }

    private boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("win");
    }

    private String resolveScriptPath(String baseCommand) {
        return baseCommand + (isWindows() ? ".bat" : ".sh");
    }

    private void setup() throws IOException {
        Path setupScript = Paths.get(resolveScriptPath(chat2apiSetupCommand));
        if (!Files.exists(setupScript)) {
            throw new IOException("Setup script not found: " + setupScript);
        }

        ProcessBuilder setupProcess = new ProcessBuilder();
        if (isWindows()) {
            setupProcess.command("cmd", "/c", "start", setupScript.toString());
        } else {
            setupProcess.command("sh", "-c", "./" + setupScript);
        }
        setupProcess.start();

        Path path = Paths.get("chat2api/app.py");
        long startTime = System.currentTimeMillis();
        long timeoutMs = 60000;

        while (System.currentTimeMillis() - startTime < timeoutMs) {
            if (Files.exists(path)) {
                return;
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IOException("Interrupted while waiting for chat2api setup", e);
            }
        }

        throw new IOException("Setup timeout - app.py not found after 1 minute. Setup may have failed.");
    }

    @PreDestroy
    public void stopChat2Api() {
        if (chat2apiProcess != null && chat2apiProcess.isAlive()) {
            log.info("Terminating Chat2API process...");

            chat2apiProcess.destroy();
            killPythonProcesses();

            log.info("Chat2API process terminated.");
        } else {
            log.info("No active Chat2API process to terminate");
        }
    }

    private void killPythonProcesses() {
        try {
            if (isWindows()) {
                new ProcessBuilder("taskkill", "/F", "/IM", "python.exe", "/T").start();
            } else {
                new ProcessBuilder("pkill", "-f", "python").start();
            }
        } catch (Exception e) {
            log.debug("Could not kill python processes: {}", e.getMessage());
        }
    }
}