package com.workoutgensvc.exercise;

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
class ExerciseRepositoryTest {

    @Autowired
    private ExerciseRepository exerciseRepository;

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
    void findByUserId_WithExistingExercises_ReturnsExercises() {
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

        List<Exercise> exercises = exerciseRepository.findByUserId(testUserId);

        assertThat(exercises).hasSize(2);
        assertThat(exercises)
                .extracting(Exercise::getName)
                .containsExactlyInAnyOrder("Push-up", "Squat");
        assertThat(exercises)
                .allMatch(exercise -> exercise.getUser().getId().equals(testUserId));
    }

    @Test
    void findByUserId_WithNoExercises_ReturnsEmptyList() {
        List<Exercise> exercises = exerciseRepository.findByUserId(testUserId);

        assertThat(exercises).isEmpty();
    }

    @Test
    void findByUserId_WithDifferentUser_ReturnsEmptyList() {
        UUID differentUserId = UUID.randomUUID();
        User differentUser = User.builder()
                .id(differentUserId)
                .username("otheruser")
                .email("other@example.com")
                .password("password")
                .build();
        userRepository.save(differentUser);

        Exercise exercise = Exercise.builder()
                .name("Push-up")
                .user(differentUser)
                .build();
        exerciseRepository.save(exercise);

        List<Exercise> exercises = exerciseRepository.findByUserId(testUserId);

        assertThat(exercises).isEmpty();
    }

    @Test
    void findByUser_WithExistingExercises_ReturnsExercises() {
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

        List<Exercise> exercises = exerciseRepository.findByUser(testUser);

        assertThat(exercises).hasSize(2);
        assertThat(exercises)
                .extracting(Exercise::getName)
                .containsExactlyInAnyOrder("Push-up", "Squat");
        assertThat(exercises)
                .allMatch(exercise -> exercise.getUser().equals(testUser));
    }

    @Test
    void findByUser_WithNoExercises_ReturnsEmptyList() {
        List<Exercise> exercises = exerciseRepository.findByUser(testUser);

        assertThat(exercises).isEmpty();
    }

    @Test
    void save_ValidExercise_PersistsCorrectly() {
        Exercise exercise = Exercise.builder()
                .name("Push-up")
                .user(testUser)
                .build();

        Exercise savedExercise = exerciseRepository.save(exercise);

        assertThat(savedExercise.getId()).isNotNull();
        assertThat(savedExercise.getName()).isEqualTo("Push-up");
        assertThat(savedExercise.getUser()).isEqualTo(testUser);

        Exercise found = exerciseRepository.findById(savedExercise.getId()).orElse(null);
        assertThat(found).isNotNull();
        assertThat(found.getName()).isEqualTo("Push-up");
        assertThat(found.getUser().getId()).isEqualTo(testUserId);
    }

    @Test
    void uniqueConstraint_NameAndUserId_PreventsDuplicates() {
        Exercise exercise1 = Exercise.builder()
                .name("Push-up")
                .user(testUser)
                .build();
        exerciseRepository.save(exercise1);

        Exercise exercise2 = Exercise.builder()
                .name("Push-up") // Same name
                .user(testUser)  // Same user
                .build();

        org.junit.jupiter.api.Assertions.assertThrows(
            Exception.class,
            () -> exerciseRepository.save(exercise2)
        );
    }
}
