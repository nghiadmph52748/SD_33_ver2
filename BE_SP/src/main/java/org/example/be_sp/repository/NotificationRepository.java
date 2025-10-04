package org.example.be_sp.repository;

import org.example.be_sp.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Integer> {
    
    // Find all notifications for a user (not deleted, ordered by newest first)
    List<Notification> findByUserIdAndDeletedOrderByCreatedAtDesc(Integer userId, Boolean deleted);
    
    // Find unread notifications for a user
    List<Notification> findByUserIdAndStatusAndDeletedOrderByCreatedAtDesc(Integer userId, Integer status, Boolean deleted);
    
    // Count unread notifications for a user
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.user.id = :userId AND n.status = 0 AND n.deleted = false")
    Long countUnreadByUserId(@Param("userId") Integer userId);
    
    // Find all notifications (for admin or system-wide notifications)
    List<Notification> findByDeletedOrderByCreatedAtDesc(Boolean deleted);
}
