package org.example.be_sp.repository;

import org.example.be_sp.entity.NotificationPreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NotificationPreferenceRepository extends JpaRepository<NotificationPreference, Integer> {
    
    Optional<NotificationPreference> findByUserIdAndDeleted(Integer userId, Boolean deleted);
    
    Optional<NotificationPreference> findByUserId(Integer userId);
}
