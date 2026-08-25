package com.workoutgensvc;

import com.workoutgensvc.exercise.Exercise;
import com.workoutgensvc.exercise.ExerciseRepository;
import com.workoutgensvc.plan.Plan;
import com.workoutgensvc.plan.PlanRepository;
import com.workoutgensvc.user.User;
import com.workoutgensvc.user.UserRepository;
import com.workoutgensvc.workout.Workout;
import com.workoutgensvc.workout.WorkoutRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class WorkoutGenSvcApplicationIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ExerciseRepository exerciseRepository;

    @Autowired
    private WorkoutRepository workoutRepository;

    @Autowired
    private PlanRepository planRepository;

    private User testUser;
    private String testUserId;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .username("integrationtestuser")
                .email("integration@example.com")
                .password("password123")
                .build();
        testUser = userRepository.save(testUser);
        testUserId = testUser.getId().toString();
    }

    @Test
    void fullIntegrationTest_ExerciseGenerationFlow() throws Exception {
        assertThat(exerciseRepository.findByUser(testUser)).isEmpty();


        Exercise exercise = Exercise.builder()
                .name("Push-up")
                .user(testUser)
                .build();
        exerciseRepository.save(exercise);

        assertThat(exerciseRepository.findByUser(testUser)).hasSize(1);
        Exercise savedExercise = exerciseRepository.findByUser(testUser).get(0);
        assertThat(savedExercise.getName()).isEqualTo("Push-up");
        assertThat(savedExercise.getUser()).isEqualTo(testUser);
    }

    @Test
    void fullIntegrationTest_WorkoutGenerationFlow() throws Exception {
        Exercise exercise1 = Exercise.builder()
                .name("Push-up")
                .user(testUser)
                .build();
        Exercise exercise2 = Exercise.builder()
                .name("Squat")
                .user(testUser)
                .build();
        exerciseRepository.save(exercise1);
        exerciseRepository.save(exercise2);

        Workout workout = Workout.builder()
                .name("Upper Body Strength")
                .user(testUser)
                .build();
        workoutRepository.save(workout);

        assertThat(workoutRepository.findByUser(testUser)).hasSize(1);
        Workout savedWorkout = workoutRepository.findByUser(testUser).get(0);
        assertThat(savedWorkout.getName()).isEqualTo("Upper Body Strength");
        assertThat(savedWorkout.getUser()).isEqualTo(testUser);

        assertThat(exerciseRepository.findByUser(testUser)).hasSize(2);
    }

    @Test
    void fullIntegrationTest_PlanGenerationFlow() throws Exception {
        Workout workout = Workout.builder()
                .name("Upper Body Strength")
                .user(testUser)
                .build();
        workoutRepository.save(workout);

        Plan plan = Plan.builder()
                .name("4-Week Strength Building Plan")
                .user(testUser)
                .build();
        planRepository.save(plan);

        assertThat(planRepository.findByUser(testUser)).hasSize(1);
        Plan savedPlan = planRepository.findByUser(testUser).get(0);
        assertThat(savedPlan.getName()).isEqualTo("4-Week Strength Building Plan");
        assertThat(savedPlan.getUser()).isEqualTo(testUser);

        assertThat(workoutRepository.findByUser(testUser)).hasSize(1);
    }

    @Test
    void duplicatePrevention_ExerciseWithSameNameAndUser_GracefulHandling() {
        Exercise exercise1 = Exercise.builder()
                .name("Push-up")
                .user(testUser)
                .build();
        exerciseRepository.save(exercise1);

        Exercise exercise2 = Exercise.builder()
                .name("Push-up") // Same name
                .user(testUser)  // Same user
                .build();

        exerciseRepository.save(exercise2);

        assertThat(exerciseRepository.findByUser(testUser)).hasSize(2);
        assertThat(exerciseRepository.findByUserId(testUser.getId())).hasSize(2);
    }

    @Test
    void duplicatePrevention_WorkoutWithSameNameAndUser_GracefulHandling() {
        Workout workout1 = Workout.builder()
                .name("Upper Body Strength")
                .user(testUser)
                .build();
        workoutRepository.save(workout1);

        Workout workout2 = Workout.builder()
                .name("Upper Body Strength") // Same name
                .user(testUser)  // Same user
                .build();

        workoutRepository.save(workout2);

        assertThat(workoutRepository.findByUser(testUser)).hasSize(2);
        assertThat(workoutRepository.findByUserId(testUser.getId())).hasSize(2);
    }

    @Test
    void duplicatePrevention_PlanWithSameNameAndUser_GracefulHandling() {
        Plan plan1 = Plan.builder()
                .name("4-Week Strength Building Plan")
                .user(testUser)
                .build();
        planRepository.save(plan1);

        Plan plan2 = Plan.builder()
                .name("4-Week Strength Building Plan") // Same name
                .user(testUser)  // Same user
                .build();

        planRepository.save(plan2);

        assertThat(planRepository.findByUser(testUser)).hasSize(2);
        assertThat(planRepository.findByUserId(testUser.getId())).hasSize(2);
    }

    @Test
    void userIsolation_ExercisesFromDifferentUsers_AreSeparate() {
        User otherUser = User.builder()
                .username("otheruser")
                .email("other@example.com")
                .password("password123")
                .build();
        otherUser = userRepository.save(otherUser);

        Exercise exercise1 = Exercise.builder()
                .name("Push-up")
                .user(testUser)
                .build();
        Exercise exercise2 = Exercise.builder()
                .name("Push-up") // Same name
                .user(otherUser) // Different user
                .build();

        exerciseRepository.save(exercise1);
        exerciseRepository.save(exercise2);

        var testUserExercises = exerciseRepository.findByUser(testUser);
        var otherUserExercises = exerciseRepository.findByUser(otherUser);

        assertThat(testUserExercises).hasSize(1);
        assertThat(otherUserExercises).hasSize(1);
        assertThat(testUserExercises.get(0).getUser()).isEqualTo(testUser);
        assertThat(otherUserExercises.get(0).getUser()).isEqualTo(otherUser);
    }

    @Test
    void cascadingRelationships_WorkoutReferencesExistingExercises() {
        Exercise exercise1 = Exercise.builder()
                .name("Push-up")
                .user(testUser)
                .build();
        Exercise exercise2 = Exercise.builder()
                .name("Squat")
                .user(testUser)
                .build();
        exerciseRepository.save(exercise1);
        exerciseRepository.save(exercise2);

        Workout workout = Workout.builder()
                .name("Full Body Workout")
                .user(testUser)
                .build();
        workoutRepository.save(workout);

        var exercises = exerciseRepository.findByUser(testUser);
        var workouts = workoutRepository.findByUser(testUser);

        assertThat(exercises).hasSize(2);
        assertThat(workouts).hasSize(1);
        assertThat(workouts.get(0).getUser()).isEqualTo(testUser);
        assertThat(exercises).allMatch(ex -> ex.getUser().equals(testUser));
    }
}
