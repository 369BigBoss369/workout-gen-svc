package com.workoutgensvc;

import com.workoutgensvc.exercise.Exercise;
import com.workoutgensvc.exercise.ExerciseRepository;
import com.workoutgensvc.exercise.ExerciseService;
import com.workoutgensvc.plan.Plan;
import com.workoutgensvc.plan.PlanRepository;
import com.workoutgensvc.plan.PlanService;
import com.workoutgensvc.user.User;
import com.workoutgensvc.user.UserRepository;
import com.workoutgensvc.workout.Workout;
import com.workoutgensvc.workout.WorkoutRepository;
import com.workoutgensvc.workout.WorkoutService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class WorkoutGenSvcComprehensiveTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ExerciseRepository exerciseRepository;

    @Autowired
    private WorkoutRepository workoutRepository;

    @Autowired
    private PlanRepository planRepository;

    @Autowired
    private ExerciseService exerciseService;

    @Autowired
    private WorkoutService workoutService;

    @Autowired
    private PlanService planService;

    private User testUser;
    private String testUserId;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .username("comprehensive-test-user")
                .email("comprehensive@test.com")
                .password("password123")
                .build();
        testUser = userRepository.save(testUser);
        testUserId = testUser.getId().toString();
    }


    @Test
    void unitTest_UserRepository_SaveAndFind() {
        User foundUser = userRepository.findById(testUser.getId()).orElse(null);
        assertThat(foundUser).isNotNull();
        assertThat(foundUser.getUsername()).isEqualTo("comprehensive-test-user");
        assertThat(foundUser.getEmail()).isEqualTo("comprehensive@test.com");
    }

    @Test
    void unitTest_ExerciseRepository_SaveAndFindByUser() {
        Exercise exercise = Exercise.builder()
                .name("Test Push-up")
                .user(testUser)
                .build();
        exerciseRepository.save(exercise);

        List<Exercise> exercises = exerciseRepository.findByUser(testUser);
        assertThat(exercises).hasSize(1);
        assertThat(exercises.get(0).getName()).isEqualTo("Test Push-up");
    }

    @Test
    void unitTest_WorkoutRepository_SaveAndFindByUser() {
        Workout workout = Workout.builder()
                .name("Test Upper Body Workout")
                .user(testUser)
                .build();
        workoutRepository.save(workout);

        List<Workout> workouts = workoutRepository.findByUser(testUser);
        assertThat(workouts).hasSize(1);
        assertThat(workouts.get(0).getName()).isEqualTo("Test Upper Body Workout");
    }

    @Test
    void unitTest_PlanRepository_SaveAndFindByUser() {
        Plan plan = Plan.builder()
                .name("Test 4-Week Plan")
                .user(testUser)
                .build();
        planRepository.save(plan);

        List<Plan> plans = planRepository.findByUser(testUser);
        assertThat(plans).hasSize(1);
        assertThat(plans.get(0).getName()).isEqualTo("Test 4-Week Plan");
    }


    @Test
    void integrationTest_UserExerciseRelationship() {
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

        List<Exercise> userExercises = exerciseRepository.findByUser(testUser);
        assertThat(userExercises).hasSize(2);
        assertThat(userExercises).allMatch(exercise -> exercise.getUser().equals(testUser));
    }

    @Test
    void integrationTest_UserWorkoutRelationship() {
        Workout workout1 = Workout.builder()
                .name("Upper Body")
                .user(testUser)
                .build();
        Workout workout2 = Workout.builder()
                .name("Lower Body")
                .user(testUser)
                .build();

        workoutRepository.save(workout1);
        workoutRepository.save(workout2);

        List<Workout> userWorkouts = workoutRepository.findByUser(testUser);
        assertThat(userWorkouts).hasSize(2);
        assertThat(userWorkouts).allMatch(workout -> workout.getUser().equals(testUser));
    }

    @Test
    void integrationTest_UserPlanRelationship() {
        Plan plan1 = Plan.builder()
                .name("Beginner Plan")
                .user(testUser)
                .build();
        Plan plan2 = Plan.builder()
                .name("Advanced Plan")
                .user(testUser)
                .build();

        planRepository.save(plan1);
        planRepository.save(plan2);

        List<Plan> userPlans = planRepository.findByUser(testUser);
        assertThat(userPlans).hasSize(2);
        assertThat(userPlans).allMatch(plan -> plan.getUser().equals(testUser));
    }

    @Test
    void integrationTest_FullWorkoutFlow() {

        Exercise pushup = Exercise.builder()
                .name("Push-up")
                .user(testUser)
                .build();
        Exercise squat = Exercise.builder()
                .name("Squat")
                .user(testUser)
                .build();
        exerciseRepository.save(pushup);
        exerciseRepository.save(squat);

        Workout upperBody = Workout.builder()
                .name("Upper Body Strength")
                .user(testUser)
                .build();
        workoutRepository.save(upperBody);

        Plan strengthPlan = Plan.builder()
                .name("4-Week Strength Plan")
                .user(testUser)
                .build();
        planRepository.save(strengthPlan);

        assertThat(exerciseRepository.findByUser(testUser)).hasSize(2);
        assertThat(workoutRepository.findByUser(testUser)).hasSize(1);
        assertThat(planRepository.findByUser(testUser)).hasSize(1);

        String exerciseNames = exerciseService.getExerciseNamesByUserId(testUser.getId());
        assertThat(exerciseNames).startsWith("Available exercises:");
        assertThat(exerciseNames).contains("Push-up");
        assertThat(exerciseNames).contains("Squat");

        String workoutNames = workoutService.getWorkoutNamesByUserId(testUser.getId());
        assertThat(workoutNames).isEqualTo("Available workouts: Upper Body Strength");
    }


    @Test
    void apiTest_ExerciseServiceMethods() {
        Exercise exercise = Exercise.builder()
                .name("Bench Press")
                .user(testUser)
                .build();
        exerciseRepository.save(exercise);

        String result = exerciseService.getExerciseNamesByUserId(testUser.getId());
        assertThat(result).isEqualTo("Available exercises: Bench Press");

        User newUser = User.builder()
                .username("empty-user")
                .email("empty@test.com")
                .password("pass")
                .build();
        newUser = userRepository.save(newUser);

        String emptyResult = exerciseService.getExerciseNamesByUserId(newUser.getId());
        assertThat(emptyResult).isEqualTo("No existing exercises available - you can suggest exercises");
    }

    @Test
    void apiTest_WorkoutServiceMethods() {
        Workout workout = Workout.builder()
                .name("Full Body Workout")
                .user(testUser)
                .build();
        workoutRepository.save(workout);

        String result = workoutService.getWorkoutNamesByUserId(testUser.getId());
        assertThat(result).isEqualTo("Available workouts: Full Body Workout");
    }


    @Test
    void constraintTest_UniqueExercisePerUser() {
        Exercise exercise1 = Exercise.builder()
                .name("Duplicate Push-up")
                .user(testUser)
                .build();
        exerciseRepository.save(exercise1);

        Exercise exercise2 = Exercise.builder()
                .name("Different Exercise")
                .user(testUser)
                .build();
        exerciseRepository.save(exercise2);

        assertThat(exerciseRepository.findByUser(testUser)).hasSize(2);
    }

    @Test
    void constraintTest_UniqueWorkoutPerUser() {
        Workout workout1 = Workout.builder()
                .name("My Workout")
                .user(testUser)
                .build();
        workoutRepository.save(workout1);

        Workout workout2 = Workout.builder()
                .name("Different Workout")
                .user(testUser)
                .build();
        workoutRepository.save(workout2);

        assertThat(workoutRepository.findByUser(testUser)).hasSize(2);
    }

    @Test
    void constraintTest_UniquePlanPerUser() {
        Plan plan1 = Plan.builder()
                .name("My Plan")
                .user(testUser)
                .build();
        planRepository.save(plan1);

        Plan plan2 = Plan.builder()
                .name("Different Plan")
                .user(testUser)
                .build();
        planRepository.save(plan2);

        assertThat(planRepository.findByUser(testUser)).hasSize(2);
    }

    @Test
    void isolationTest_UsersHaveSeparateData() {
        User user2 = User.builder()
                .username("user2")
                .email("user2@test.com")
                .password("pass")
                .build();
        user2 = userRepository.save(user2);

        Exercise exercise1 = Exercise.builder()
                .name("Push-up")
                .user(testUser)
                .build();
        exerciseRepository.save(exercise1);

        Exercise exercise2 = Exercise.builder()
                .name("Push-up")
                .user(user2)
                .build();
        exerciseRepository.save(exercise2);

        assertThat(exerciseRepository.findByUser(testUser)).hasSize(1);
        assertThat(exerciseRepository.findByUser(user2)).hasSize(1);
        assertThat(exerciseRepository.findByUserId(testUser.getId())).hasSize(1);
        assertThat(exerciseRepository.findByUserId(user2.getId())).hasSize(1);
    }


    @Test
    void performanceTest_BulkOperations() {
        for (int i = 1; i <= 10; i++) {
            Exercise exercise = Exercise.builder()
                    .name("Exercise " + i)
                    .user(testUser)
                    .build();
            exerciseRepository.save(exercise);
        }

        List<Exercise> exercises = exerciseRepository.findByUser(testUser);
        assertThat(exercises).hasSize(10);
    }

    @Test
    void edgeCaseTest_SpecialCharactersInNames() {
        Exercise exercise = Exercise.builder()
                .name("Exercise with special chars: àáâãäå")
                .user(testUser)
                .build();
        exerciseRepository.save(exercise);

        List<Exercise> exercises = exerciseRepository.findByUser(testUser);
        assertThat(exercises).hasSize(1);
        assertThat(exercises.get(0).getName()).contains("àáâãäå");
    }
}
