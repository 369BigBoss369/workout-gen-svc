package com.workoutgensvc.plan;

import com.workoutgensvc.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PlanRepository extends JpaRepository<Plan, UUID> {
    List<Plan> findByUserId(UUID userId);
    List<Plan> findByUser(User user);
}
