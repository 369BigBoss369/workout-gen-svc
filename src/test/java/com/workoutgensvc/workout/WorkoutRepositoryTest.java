package com.workoutgensvc.workout;

import com.workoutgensvc.user.User;
import com.workoutgensvc.user.UserRepository;
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
class WorkoutRepositoryTest {

    @Autowired
    private WorkoutRepository workoutRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private UUID testUserId;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testUser = User.builder()
                .id(testUserId)
                .username("testuser" + testUserId.toString().substring(0, 8))
                .email("test" + testUserId.toString().substring(0, 8) + "@example.com")
                .password("password")
                .build();
        testUser = userRepository.save(testUser);
    }

    @Test
    void findByUserId_WithExistingWorkouts_ReturnsWorkouts() {
        Workout workout1 = Workout.builder()
                .name("Upper Body Strength")
                .user(testUser)
                .build();
        Workout workout2 = Workout.builder()
                .name("Lower Body Power")
                .user(testUser)
                .build();

        workoutRepository.save(workout1);
        workoutRepository.save(workout2);

        List<Workout> workouts = workoutRepository.findByUserId(testUserId);

        assertThat(workouts).hasSize(2);
        assertThat(workouts)
                .extracting(Workout::getName)
                .containsExactlyInAnyOrder("Upper Body Strength", "Lower Body Power");
        assertThat(workouts)
                .allMatch(workout -> workout.getUser().getId().equals(testUserId));
    }

    @Test
    void findByUserId_WithNoWorkouts_ReturnsEmptyList() {
        List<Workout> workouts = workoutRepository.findByUserId(testUserId);

        assertThat(workouts).isEmpty();
    }

    @Test
    void findByUser_WithExistingWorkouts_ReturnsWorkouts() {
        Workout workout1 = Workout.builder()
                .name("Upper Body Strength")
                .user(testUser)
                .build();
        Workout workout2 = Workout.builder()
                .name("Lower Body Power")
                .user(testUser)
                .build();

        workoutRepository.save(workout1);
        workoutRepository.save(workout2);

        List<Workout> workouts = workoutRepository.findByUser(testUser);

        assertThat(workouts).hasSize(2);
        assertThat(workouts)
                .extracting(Workout::getName)
                .containsExactlyInAnyOrder("Upper Body Strength", "Lower Body Power");
        assertThat(workouts)
                .allMatch(workout -> workout.getUser().equals(testUser));
    }

    @Test
    void save_ValidWorkout_PersistsCorrectly() {
        Workout workout = Workout.builder()
                .name("Upper Body Strength")
                .user(testUser)
                .build();

        Workout savedWorkout = workoutRepository.save(workout);

        assertThat(savedWorkout.getId()).isNotNull();
        assertThat(savedWorkout.getName()).isEqualTo("Upper Body Strength");
        assertThat(savedWorkout.getUser()).isEqualTo(testUser);

        Workout found = workoutRepository.findById(savedWorkout.getId()).orElse(null);
        assertThat(found).isNotNull();
        assertThat(found.getName()).isEqualTo("Upper Body Strength");
        assertThat(found.getUser().getId()).isEqualTo(testUserId);
    }

    @Test
    void uniqueConstraint_NameAndUserId_PreventsDuplicates() {
        Workout workout1 = Workout.builder()
                .name("Upper Body Strength")
                .user(testUser)
                .build();
        workoutRepository.save(workout1);

        Workout workout2 = Workout.builder()
                .name("Upper Body Strength") // Same name
                .user(testUser)  // Same user
                .build();

        org.junit.jupiter.api.Assertions.assertThrows(
            Exception.class,
            () -> workoutRepository.save(workout2)
        );
    }
}
