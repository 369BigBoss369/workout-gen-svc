package com.workoutgensvc.plan;

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
class PlanRepositoryTest {

    @Autowired
    private PlanRepository planRepository;

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
    void findByUserId_WithExistingPlans_ReturnsPlans() {
        Plan plan1 = Plan.builder()
                .name("4-Week Strength Building Plan")
                .user(testUser)
                .build();
        Plan plan2 = Plan.builder()
                .name("8-Week Endurance Program")
                .user(testUser)
                .build();

        planRepository.save(plan1);
        planRepository.save(plan2);

        List<Plan> plans = planRepository.findByUserId(testUserId);

        assertThat(plans).hasSize(2);
        assertThat(plans)
                .extracting(Plan::getName)
                .containsExactlyInAnyOrder("4-Week Strength Building Plan", "8-Week Endurance Program");
        assertThat(plans)
                .allMatch(plan -> plan.getUser().getId().equals(testUserId));
    }

    @Test
    void findByUserId_WithNoPlans_ReturnsEmptyList() {
        List<Plan> plans = planRepository.findByUserId(testUserId);

        assertThat(plans).isEmpty();
    }

    @Test
    void findByUser_WithExistingPlans_ReturnsPlans() {
        Plan plan1 = Plan.builder()
                .name("4-Week Strength Building Plan")
                .user(testUser)
                .build();
        Plan plan2 = Plan.builder()
                .name("8-Week Endurance Program")
                .user(testUser)
                .build();

        planRepository.save(plan1);
        planRepository.save(plan2);

        List<Plan> plans = planRepository.findByUser(testUser);

        assertThat(plans).hasSize(2);
        assertThat(plans)
                .extracting(Plan::getName)
                .containsExactlyInAnyOrder("4-Week Strength Building Plan", "8-Week Endurance Program");
        assertThat(plans)
                .allMatch(plan -> plan.getUser().equals(testUser));
    }

    @Test
    void save_ValidPlan_PersistsCorrectly() {
        Plan plan = Plan.builder()
                .name("4-Week Strength Building Plan")
                .user(testUser)
                .build();

        Plan savedPlan = planRepository.save(plan);

        assertThat(savedPlan.getId()).isNotNull();
        assertThat(savedPlan.getName()).isEqualTo("4-Week Strength Building Plan");
        assertThat(savedPlan.getUser()).isEqualTo(testUser);

        Plan found = planRepository.findById(savedPlan.getId()).orElse(null);
        assertThat(found).isNotNull();
        assertThat(found.getName()).isEqualTo("4-Week Strength Building Plan");
        assertThat(found.getUser().getId()).isEqualTo(testUserId);
    }

    @Test
    void uniqueConstraint_NameAndUserId_PreventsDuplicates() {
        Plan plan1 = Plan.builder()
                .name("4-Week Strength Building Plan")
                .user(testUser)
                .build();
        planRepository.save(plan1);

        Plan plan2 = Plan.builder()
                .name("4-Week Strength Building Plan") // Same name
                .user(testUser)  // Same user
                .build();

        org.junit.jupiter.api.Assertions.assertThrows(
            Exception.class,
            () -> planRepository.save(plan2)
        );
    }
}
