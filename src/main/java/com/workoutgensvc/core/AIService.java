package com.workoutgensvc.core;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class AIService {
    private final String model;
    private final String chat2apiBaseUrl;
    private final String chat2apiAccessToken;
    private RestTemplate restTemplate;

    public AIService(@Value("${openai.api.model}") String model, @Value("${chat2api.base-url}") String chat2apiBaseUrl, @Value("${chat2api.access-token}") String chat2apiAccessToken) {
        this.model = model;
        this.chat2apiBaseUrl = chat2apiBaseUrl;
        this.chat2apiAccessToken = chat2apiAccessToken;
        initializeRestTemplate();
    }

    private void initializeRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(7000);
        factory.setReadTimeout(30000);
        this.restTemplate = new RestTemplate(factory);
    }

    void setRestTemplate(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String generateResponse(String prompt) {
        try {
            return generateResponseViaChat2Api(prompt);
        } catch (Exception e) {
            log.error("Chat2API request failed: {}", e.getMessage());

            if (e.getMessage().contains("Connection refused") || e.getMessage().contains("Connection timed out")) {
                throw new RuntimeException("Chat2API server is not running!");
            }

            throw new RuntimeException("AI service unavailable: " + e.getMessage(), e);
        }
    }

    private String generateResponseViaChat2Api(String prompt) {
        try {

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);
            requestBody.put("messages", List.of(Map.of("role", "user", "content", prompt)));
            requestBody.put("max_tokens", 5000);
            requestBody.put("temperature", 1.0);
            requestBody.put("stream", false);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            if (chat2apiAccessToken != null && !chat2apiAccessToken.trim().isEmpty()) {
                headers.setBearerAuth(chat2apiAccessToken);
                log.debug("Using Chat2API with Authorization header.");
            } else {
                log.debug("Using Chat2API without Authorization header.");
            }

            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

            String url = chat2apiBaseUrl + "/v1/chat/completions";
            long startTime = System.currentTimeMillis();
            ResponseEntity<String> response = restTemplate.postForEntity(url, requestEntity, String.class);
            long endTime = System.currentTimeMillis();

            log.debug("Request completed in {} ms", (endTime - startTime));
            log.debug("Chat2API response status: {}", response.getStatusCode());

            if (response.getStatusCode() != HttpStatus.OK) {
                throw new RuntimeException("Chat2API request failed with status: " + response.getStatusCode());
            }

            String responseJson = response.getBody();
            JsonObject rootObject = JsonParser.parseString(responseJson).getAsJsonObject();

            if (rootObject.has("choices") && rootObject.get("choices").isJsonArray()) {
                JsonArray choices = rootObject.get("choices").getAsJsonArray();

                if (!choices.isEmpty()) {
                    JsonObject firstChoice = choices.get(0).getAsJsonObject();

                    if (firstChoice.has("message") && firstChoice.get("message").isJsonObject()) {
                        JsonObject messageObject = firstChoice.get("message").getAsJsonObject();

                        if (messageObject.has("content")) {
                            return messageObject.get("content").getAsString();
                        }
                    }
                }
            }
            throw new RuntimeException("Invalid response format from chat2api");

        } catch (Exception e) {
            log.error("Chat2API request failed: {}", e.getMessage(), e);
            throw new RuntimeException("Chat2API service unavailable: " + e.getMessage(), e);
        }
    }

    // NEW
    public String generateValidExercise(String muscleGroup, String difficulty, String equipment, int maxRetries) {
        log.info("Generating valid exercise - muscleGroup: {}, difficulty: {}, equipment: {}, maxRetries: {}", muscleGroup, difficulty, equipment, maxRetries);
        int attempts = 0;
        String lastAttemptJson = null;

        while (attempts < maxRetries) {
            try {
                log.debug("Attempt {} of {} to generate valid exercise", attempts + 1, maxRetries);
                String exerciseJson = generateExercise(muscleGroup, difficulty, equipment);
                lastAttemptJson = exerciseJson;
                JsonObject exercise = JsonParser.parseString(exerciseJson).getAsJsonObject();

                boolean hasValidUrl = false;

                if (exercise.has("imageUrl") && !exercise.get("imageUrl").isJsonNull()) {
                    String imageUrl = exercise.get("imageUrl").getAsString();
                    if (isValidUrl(imageUrl)) {
                        hasValidUrl = true;
                    }
                }

                if (!hasValidUrl && exercise.has("videoUrl") && !exercise.get("videoUrl").isJsonNull()) {
                    String videoUrl = exercise.get("videoUrl").getAsString();
                    if (isValidUrl(videoUrl)) {
                        hasValidUrl = true;
                    }
                }

                if (hasValidUrl) {
                    log.info("Successfully generated exercise with valid URLs on attempt {}", attempts + 1);
                    return exerciseJson;
                } else {
                    log.warn("Generated exercise missing valid URLs, retrying... (attempt {}/{})", attempts + 1, maxRetries);
                    attempts++;
                }

            } catch (Exception e) {
                log.warn("Error generating exercise on attempt {}: {}", attempts + 1, e.getMessage());
                attempts++;
            }
        }

        log.error("Failed to generate exercise with valid URLs after {} attempts, returning last attempt", maxRetries);
        if (lastAttemptJson == null) {
            throw new RuntimeException("Failed to generate exercise after " + maxRetries + " attempts");
        }
        return lastAttemptJson;
    }

    public String generateExercise(String muscleGroup, String difficulty, String equipment) {
        log.debug("Generating exercise prompt for muscleGroup: {}, difficulty: {}, equipment: {}", muscleGroup, difficulty, equipment);
        String prompt = String.format(
                "You are an expert fitness coach and exercise database generator.\n" +
                        "\n" +
                        "Generate ONE exercise that targets the muscle group: \"%s\".\n" +
                        "Difficulty level: \"%s\".\n" +
                        "Available equipment: \"%s\".\n" +
                        "\n" +
                        "REQUIREMENTS:\n" +
                        "You MUST return VALID JSON ONLY. No extra text.\n" +
                        "All fields must match EXACTLY the schema below.\n" +
                        "\"name\" must be a real exercise name.\n" +
                        "\"type\" must be one of: STRENGTH, PLYOMETRIC, CARDIO, AGILITY, BALANCE, FLEXIBILITY, RECOVERY.\n" +
                        "\"muscleTargets\" must include MULTIPLE relevant muscles that are worked by this exercise.\n" +
                        "Each muscle name must be chosen from: UPPER_CHEST, MIDDLE_CHEST, LOWER_CHEST, LATS, FRONT_DELTOID, REAR_DELTOID, LONG_HEAD_TRICEPS, SHORT_HEAD_BICEPS, QUADRICEPS, HAMSTRINGS, GLUTES, UPPER_ABS, OBLIQUES.\n" +
                        "Intensity for each muscle must be LOW, MODERATE, HIGH, or EXTREME.\n" +
                        "IMPORTANT: You MUST provide either an imageUrl OR a videoUrl (or both).\n" +
                        "Both URLs MUST BE VALID DIRECT LINKS.\n" +
                        "Do NOT provide null, empty strings, placeholders, or fake example.com links.\n" +
                        "The links MUST point to actual existing media files (real images or real videos).\n" +
                        "If one link is invalid or inaccessible, the entire output is considered invalid.\n" +
                        "At least one of them must be fully real and functional.\n" +
                        "No explanations. Only return JSON.\n" +
                        "\n" +
                        "JSON SCHEMA:\n" +
                        "{\n" +
                        "  \"name\": \"Exercise Name\",\n" +
                        "  \"type\": \"STRENGTH|PLYOMETRIC|CARDIO|AGILITY|BALANCE|FLEXIBILITY|RECOVERY\",\n" +
                        "  \"muscleTargets\": [\n" +
                        "    {\n" +
                        "      \"muscle\": \"UPPER_CHEST|MIDDLE_CHEST|LOWER_CHEST|LATS|FRONT_DELTOID|REAR_DELTOID|LONG_HEAD_TRICEPS|SHORT_HEAD_BICEPS|QUADRICEPS|HAMSTRINGS|GLUTES|UPPER_ABS|OBLIQUES\",\n" +
                        "      \"intensity\": \"LOW|MODERATE|HIGH|EXTREME\"\n" +
                        "    }\n" +
                        "  ],\n" +
                        "  \"imageUrl\": \"URL to an image (provide if no video)\",\n" +
                        "  \"videoUrl\": \"URL to a video (provide if no image)\"\n" +
                        "}"
                , muscleGroup, difficulty, equipment
        );

        return generateResponse(prompt);
    }

    public String generateWorkout(String type, String duration, String fitnessLevel, String goals, String existingExercises) {
        log.debug("Generating workout prompt - type: {}, duration: {}, fitnessLevel: {}, goals: {}", type, duration, fitnessLevel, goals);
        String prompt = String.format(
                "You are an expert fitness coach and workout planner.\n" +
                        "\n" +
                        "Create a complete workout with the following specifications:\n" +
                        "Type: \"%s\" (must be one of: STRENGTH, BODY_WEIGHT, FULL_BODY, HIIT, CARDIO, MOBILITY, YOGA)\n" +
                        "Duration: \"%s\" minutes\n" +
                        "Fitness Level: \"%s\"\n" +
                        "Goals: \"%s\"\n\n" +
                        "Available exercises:\n%s\n\n" +
                        "REQUIREMENTS:\n" +
                        "You MUST return VALID JSON ONLY. No extra text.\n" +
                        "The workout must use only the exercises listed above.\n" +
                        "All fields must match EXACTLY the schema below.\n" +
                        "\"name\" must be a realistic workout name.\n" +
                        "\"type\" must match one of the allowed types.\n" +
                        "Each exercise must have a valid \"exerciseName\" from the provided list.\n" +
                        "\"number\" must be a positive integer indicating exercise order.\n" +
                        "\"reps\" must be a positive integer (omit if the exercise is timed).\n" +
                        "\"weight\" must be a non-negative number (0 if bodyweight or no weight).\n" +
                        "\"duration\" for each exercise must be in seconds, and the sum of all exercise durations must exactly equal the total workout duration in seconds (%s minutes * 60).\n" +
                        "Adjust reps, sets, or rest periods if necessary to meet this total duration.\n" +
                        "\"burnedCalories\" must be a realistic positive number.\n" +
                        "No fields should be null or missing.\n" +
                        "No explanations. Only return JSON.\n" +
                        "\n" +
                        "JSON SCHEMA:\n" +
                        "{\n" +
                        "  \"name\": \"Workout Name\",\n" +
                        "  \"type\": \"STRENGTH|BODY_WEIGHT|FULL_BODY|HIIT|CARDIO|MOBILITY|YOGA\",\n" +
                        "  \"workoutExercises\": [\n" +
                        "    {\n" +
                        "      \"exerciseName\": \"Choose from available exercises above\",\n" +
                        "      \"number\": 1,\n" +
                        "      \"reps\": 12,\n" +
                        "      \"weight\": 25.5,\n" +
                        "      \"duration\": 45,\n" +
                        "      \"burnedCalories\": 85.5\n" +
                        "    }\n" +
                        "  ]\n" +
                        "}\n",
                type, duration, fitnessLevel, goals, existingExercises, duration
        );

        return generateResponse(prompt);
    }

    public String generatePlan(String duration, String frequency, String goals, String experience, String existingWorkouts) {
        log.debug("Generating plan prompt - duration: {}, frequency: {}, goals: {}, experience: {}", duration, frequency, goals, experience);
        String prompt = String.format(
                "You are an expert fitness coach and program planner.\n\n" +
                        "Create a comprehensive %s-week workout plan with these specifications:\n" +
                        "- Training Frequency: %s days per week\n" +
                        "- Goals: %s (must match one of: STRENGTH, WEIGHT_LOSS, ENDURANCE, FLEXIBILITY)\n" +
                        "- Experience Level: %s\n\n" +
                        "Available workouts:\n%s\n\n" +
                        "REQUIREMENTS:\n" +
                        "- You MUST return VALID JSON ONLY. No extra text.\n" +
                        "- The plan must include all days in each week. For days without workouts, use type REST.\n" +
                        "- Each day must have a dayNumber starting from 1 and increasing sequentially across all weeks.\n" +
                        "- \"type\" for each day must be one of: ACTIVE, RECOVERY, REST.\n" +
                        "- \"workoutNames\" must include only workouts from the provided list.\n" +
                        "- Make the plan realistic in terms of frequency, recovery, and progression.\n" +
                        "- Do NOT leave empty, null, or missing fields.\n\n" +
                        "JSON SCHEMA:\n" +
                        "{\n" +
                        "  \"name\": \"Plan Name\",\n" +
                        "  \"description\": \"Short overview of the plan\",\n" +
                        "  \"type\": \"STRENGTH|WEIGHT_LOSS|ENDURANCE|FLEXIBILITY\",\n" +
                        "  \"planDays\": [\n" +
                        "    {\n" +
                        "      \"dayNumber\": 1,\n" +
                        "      \"type\": \"ACTIVE|RECOVERY|REST\",\n" +
                        "      \"workoutNames\": [\"Choose from available workouts above\"]\n" +
                        "    },\n" +
                        "    {\n" +
                        "      \"dayNumber\": 2,\n" +
                        "      \"type\": \"ACTIVE|RECOVERY|REST\",\n" +
                        "      \"workoutNames\": [\"Choose from available workouts above\"]\n" +
                        "    }\n" +
                        "    // Continue for all days according to frequency and duration\n" +
                        "  ]\n" +
                        "}\n",
                duration, frequency, goals, experience, existingWorkouts
        );

        return generateResponse(prompt);
    }

    public boolean isValidUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            return false;
        }

        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("HEAD");
            connection.setConnectTimeout(4000);
            connection.setReadTimeout(4000);
            connection.setInstanceFollowRedirects(true);
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");

            int responseCode = connection.getResponseCode();

            if (200 <= responseCode && responseCode < 400) {
                String contentType = connection.getContentType();

                if (contentType != null) {
                    String lowerContentType = contentType.toLowerCase();

                    if (url.contains(".jpg") || url.contains(".jpeg") || url.contains(".png") || url.contains(".gif") || url.contains(".webp")) {
                        return lowerContentType.contains("image/");
                    } else if (url.contains(".mp4") || url.contains(".avi") || url.contains(".mov") || url.contains(".webm")) {
                        return lowerContentType.contains("video/");
                    }
                }

                return true;
            }

            return false;
        } catch (Exception e) {
            return false;
        }
    }
}