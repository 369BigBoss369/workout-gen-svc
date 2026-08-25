package com.workoutgensvc;

import com.workoutgensvc.exercise.ExerciseService;
import com.workoutgensvc.plan.PlanService;
import com.workoutgensvc.workout.WorkoutService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class WorkoutGenSvcApplicationTests {

    @Autowired
    private ExerciseService exerciseService;

    @Autowired
    private WorkoutService workoutService;

    @Autowired
    private PlanService planService;

    @Test
    void contextLoads() {
        assertThat(exerciseService).isNotNull();
        assertThat(workoutService).isNotNull();
        assertThat(planService).isNotNull();
    }

    @Test
    void applicationStartsSuccessfully() {
        assertThat(exerciseService).isNotNull();
        assertThat(workoutService).isNotNull();
        assertThat(planService).isNotNull();
    }

    @Test
    void serviceDependenciesAreInjected() {
        assertThat(exerciseService).isNotNull();
        assertThat(workoutService).isNotNull();
        assertThat(planService).isNotNull();
    }

}
