package com.workoutgensvc.plan;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.workoutgensvc.core.AIService;
import com.workoutgensvc.user.User;
import com.workoutgensvc.user.UserService;
import com.workoutgensvc.workout.WorkoutService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
public class PlanService {
    private final PlanRepository planRepository;
    private final UserService userService;
    private final WorkoutService workoutService;
    private final AIService aiService;
    private final Gson gson;

    @Autowired
    public PlanService(PlanRepository planRepository, UserService userService, WorkoutService workoutService, AIService aiService, Gson gson) {
        this.planRepository = planRepository;
        this.userService = userService;
        this.workoutService = workoutService;
        this.aiService = aiService;
        this.gson = gson;
    }

    @Transactional
    public String generatePlan(String duration, String frequency, String goals, String experience, String userId, String availableWorkouts) {
        log.info("Generating plan for user {} - duration: {}, frequency: {}, goals: {}, experience: {}", userId, duration, frequency, goals, experience);

        UUID parsedUserId = UUID.fromString(userId);

        try {
            String response = aiService.generatePlan(duration, frequency, goals, experience, availableWorkouts);
            response = response.replaceAll("```", "").replace("json\n", "");

            JsonObject jsonObject = gson.fromJson(response, JsonObject.class);
            String planName = jsonObject.get("name").getAsString();
            log.debug("AI generated plan: {}", planName);

            User user = userService.findOrCreateUser(parsedUserId, userId);

            Plan plan = Plan.builder()
                    .name(planName)
                    .user(user)
                    .build();
            planRepository.save(plan);
            log.info("Successfully saved new plan '{}' for user {}", planName, userId);

            return response;
        } catch (DataIntegrityViolationException e) {
            throw new RuntimeException("A plan with this name already exists for this user", e);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate plan from AI response: " + e.getMessage(), e);
        }
    }
}