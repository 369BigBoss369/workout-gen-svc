package com.workoutgensvc;

import com.workoutgensvc.exercise.ExerciseRepository;
import com.workoutgensvc.plan.PlanRepository;
import com.workoutgensvc.user.User;
import com.workoutgensvc.user.UserRepository;
import com.workoutgensvc.workout.WorkoutRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class WorkoutGenSvcRestIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ExerciseRepository exerciseRepository;

    @Autowired
    private WorkoutRepository workoutRepository;

    @Autowired
    private PlanRepository planRepository;

    private MockMvc mockMvc;
    private User testUser;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        planRepository.deleteAll();
        workoutRepository.deleteAll();
        exerciseRepository.deleteAll();
        userRepository.deleteAll();

        testUser = User.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password")
                .build();
        testUser = userRepository.save(testUser);
    }

    @Test
    void exerciseEndpoint_ShouldHandleDuplicateNamesGracefully() throws Exception {
        mockMvc.perform(post("/api/v1/ai/exercises")
                        .param("muscleGroup", "chest")
                        .param("difficulty", "beginner")
                        .param("equipment", "bodyweight")
                        .param("userId", testUser.getId().toString()))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/ai/exercises")
                        .param("muscleGroup", "chest")
                        .param("difficulty", "beginner")
                        .param("equipment", "bodyweight")
                        .param("userId", testUser.getId().toString()))
                .andExpect(status().isOk());
    }

    @Test
    void workoutEndpoint_ShouldHandleDuplicateNamesGracefully() throws Exception {
        mockMvc.perform(post("/api/v1/ai/exercises")
                        .param("muscleGroup", "chest")
                        .param("difficulty", "beginner")
                        .param("equipment", "bodyweight")
                        .param("userId", testUser.getId().toString()))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/ai/workouts")
                        .param("type", "upper body")
                        .param("duration", "30 minutes")
                        .param("fitnessLevel", "beginner")
                        .param("goals", "build strength")
                        .param("userId", testUser.getId().toString()))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/ai/workouts")
                        .param("type", "upper body")
                        .param("duration", "30 minutes")
                        .param("fitnessLevel", "beginner")
                        .param("goals", "build strength")
                        .param("userId", testUser.getId().toString()))
                .andExpect(status().isOk());
    }

    @Test
    void planEndpoint_ShouldHandleDuplicateNamesGracefully() throws Exception {
        mockMvc.perform(post("/api/v1/ai/exercises")
                        .param("muscleGroup", "chest")
                        .param("difficulty", "beginner")
                        .param("equipment", "bodyweight")
                        .param("userId", testUser.getId().toString()))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/ai/workouts")
                        .param("type", "upper body")
                        .param("duration", "30 minutes")
                        .param("fitnessLevel", "beginner")
                        .param("goals", "build strength")
                        .param("userId", testUser.getId().toString()))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/ai/plans")
                        .param("duration", "4 weeks")
                        .param("frequency", "3 days/week")
                        .param("goals", "build strength")
                        .param("experience", "beginner")
                        .param("userId", testUser.getId().toString()))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/ai/plans")
                        .param("duration", "4 weeks")
                        .param("frequency", "3 days/week")
                        .param("goals", "build strength")
                        .param("experience", "beginner")
                        .param("userId", testUser.getId().toString()))
                .andExpect(status().isOk());
    }

    @Test
    void endpoints_ShouldReturnBadRequest_ForInvalidUserId() throws Exception {
        mockMvc.perform(post("/api/v1/ai/exercises")
                        .param("muscleGroup", "chest")
                        .param("difficulty", "beginner")
                        .param("equipment", "bodyweight")
                        .param("userId", "invalid-uuid"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/v1/ai/workouts")
                        .param("type", "upper body")
                        .param("duration", "30 minutes")
                        .param("fitnessLevel", "beginner")
                        .param("goals", "build strength")
                        .param("userId", "invalid-uuid"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/v1/ai/plans")
                        .param("duration", "4 weeks")
                        .param("frequency", "3 days/week")
                        .param("goals", "build strength")
                        .param("experience", "beginner")
                        .param("userId", "invalid-uuid"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void endpoints_ShouldReturnInternalServerError_ForNonexistentUser() throws Exception {
        String nonexistentUserId = UUID.randomUUID().toString();

        mockMvc.perform(post("/api/v1/ai/exercises")
                        .param("muscleGroup", "chest")
                        .param("difficulty", "beginner")
                        .param("equipment", "bodyweight")
                        .param("userId", nonexistentUserId))
                .andExpect(status().is5xxServerError());

        mockMvc.perform(post("/api/v1/ai/workouts")
                        .param("type", "upper body")
                        .param("duration", "30 minutes")
                        .param("fitnessLevel", "beginner")
                        .param("goals", "build strength")
                        .param("userId", nonexistentUserId))
                .andExpect(status().is5xxServerError());

        mockMvc.perform(post("/api/v1/ai/plans")
                        .param("duration", "4 weeks")
                        .param("frequency", "3 days/week")
                        .param("goals", "build strength")
                        .param("experience", "beginner")
                        .param("userId", nonexistentUserId))
                .andExpect(status().is5xxServerError());
    }
}
