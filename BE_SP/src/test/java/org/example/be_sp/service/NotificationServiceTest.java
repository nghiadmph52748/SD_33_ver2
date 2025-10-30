package org.example.be_sp.service;

import org.example.be_sp.controller.NotificationWebSocketController;
import org.example.be_sp.entity.NhanVien;
import org.example.be_sp.entity.Notification;
import org.example.be_sp.exception.ApiException;
import org.example.be_sp.model.response.NotificationResponse;
import org.example.be_sp.repository.NhanVienRepository;
import org.example.be_sp.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Notification Service Tests")
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private NhanVienRepository nhanVienRepository;

    @Mock
    private NotificationWebSocketController webSocketController;

    @InjectMocks
    private NotificationService notificationService;

    private NhanVien testUser;
    private Notification testNotification;

    @BeforeEach
    void setUp() {
        testUser = new NhanVien();
        testUser.setId(1);
        testUser.setTenTaiKhoan("testuser");
        testUser.setTenNhanVien("Test User");

        testNotification = new Notification();
        testNotification.setId(1);
        testNotification.setType("notice");
        testNotification.setTitle("Test Notification");
        testNotification.setSubTitle("Test Subtitle");
        testNotification.setContent("Test content");
        testNotification.setStatus(0);
        testNotification.setMessageType(3);
        testNotification.setUser(testUser);
        testNotification.setDeleted(false);
        testNotification.setCreatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("Should get all notifications for a user")
    void testGetNotificationsForUser_Success() {
        // Arrange
        List<Notification> notifications = Arrays.asList(testNotification);
        when(notificationRepository.findByUserIdAndDeletedOrderByCreatedAtDesc(1, false))
                .thenReturn(notifications);

        // Act
        List<NotificationResponse> result = notificationService.getNotificationsForUser(1);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Notification", result.get(0).getTitle());
        verify(notificationRepository).findByUserIdAndDeletedOrderByCreatedAtDesc(1, false);
    }

    @Test
    @DisplayName("Should return empty list when user has no notifications")
    void testGetNotificationsForUser_EmptyList() {
        // Arrange
        when(notificationRepository.findByUserIdAndDeletedOrderByCreatedAtDesc(1, false))
                .thenReturn(Arrays.asList());

        // Act
        List<NotificationResponse> result = notificationService.getNotificationsForUser(1);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    @DisplayName("Should get unread notification count for a user")
    void testGetUnreadCount_Success() {
        // Arrange
        when(notificationRepository.countUnreadByUserId(1)).thenReturn(5L);

        // Act
        Long count = notificationService.getUnreadCount(1);

        // Assert
        assertEquals(5L, count);
        verify(notificationRepository).countUnreadByUserId(1);
    }

    @Test
    @DisplayName("Should mark multiple notifications as read")
    void testMarkAsRead_Success() {
        // Arrange
        List<Integer> ids = Arrays.asList(1, 2);
        when(notificationRepository.findById(1)).thenReturn(Optional.of(testNotification));
        
        Notification notification2 = new Notification();
        notification2.setId(2);
        notification2.setStatus(0);
        when(notificationRepository.findById(2)).thenReturn(Optional.of(notification2));

        // Act
        notificationService.markAsRead(ids);

        // Assert
        assertEquals(1, testNotification.getStatus());
        verify(notificationRepository, times(2)).save(any(Notification.class));
    }

    @Test
    @DisplayName("Should throw exception when notification not found")
    void testMarkAsRead_NotificationNotFound() {
        // Arrange
        List<Integer> ids = Arrays.asList(999);
        when(notificationRepository.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ApiException.class, () -> notificationService.markAsRead(ids));
    }

    @Test
    @DisplayName("Should create notification and push via WebSocket")
    void testCreateNotification_Success() {
        // Arrange
        when(nhanVienRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);
        doNothing().when(webSocketController).sendNotificationToUser(anyInt(), any());

        // Act
        Notification result = notificationService.createNotification(
                1, "notice", "Test Title", "Test Subtitle", "Test Content", 3
        );

        // Assert
        assertNotNull(result);
        assertEquals("Test Notification", result.getTitle());
        verify(nhanVienRepository).findById(1);
        verify(notificationRepository).save(any(Notification.class));
        verify(webSocketController).sendNotificationToUser(eq(1), any(NotificationResponse.class));
    }

    @Test
    @DisplayName("Should throw exception when user not found")
    void testCreateNotification_UserNotFound() {
        // Arrange
        when(nhanVienRepository.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ApiException.class, () -> 
            notificationService.createNotification(999, "notice", "Title", "Subtitle", "Content", 3)
        );
        verify(notificationRepository, never()).save(any());
        verify(webSocketController, never()).sendNotificationToUser(anyInt(), any());
    }

    @Test
    @DisplayName("Should create broadcast notification for all users")
    void testCreateBroadcastNotification_Success() {
        // Arrange
        List<NhanVien> users = Arrays.asList(testUser);
        when(nhanVienRepository.findAll()).thenReturn(users);
        when(nhanVienRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);

        // Act
        notificationService.createBroadcastNotification("notice", "Broadcast", "Subtitle", "Content", 3);

        // Assert
        verify(nhanVienRepository).findAll();
        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    @DisplayName("Should soft delete a notification")
    void testDeleteNotification_Success() {
        // Arrange
        when(notificationRepository.findById(1)).thenReturn(Optional.of(testNotification));

        // Act
        notificationService.deleteNotification(1);

        // Assert
        assertTrue(testNotification.getDeleted());
        verify(notificationRepository).save(testNotification);
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent notification")
    void testDeleteNotification_NotFound() {
        // Arrange
        when(notificationRepository.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ApiException.class, () -> notificationService.deleteNotification(999));
    }

    @Test
    @DisplayName("Should clear all notifications for a user")
    void testClearAllForUser_Success() {
        // Arrange
        List<Notification> notifications = Arrays.asList(testNotification);
        when(notificationRepository.findByUserIdAndDeletedOrderByCreatedAtDesc(1, false))
                .thenReturn(notifications);

        // Act
        notificationService.clearAllForUser(1);

        // Assert
        assertTrue(testNotification.getDeleted());
        verify(notificationRepository).save(testNotification);
    }

    @Test
    @DisplayName("Should handle WebSocket failure gracefully without throwing exception")
    void testCreateNotification_WebSocketFailure_ShouldNotThrow() {
        // Arrange
        when(nhanVienRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);
        doThrow(new RuntimeException("WebSocket error"))
                .when(webSocketController).sendNotificationToUser(anyInt(), any());

        // Act & Assert - Should not throw exception
        assertDoesNotThrow(() -> 
            notificationService.createNotification(1, "notice", "Title", "Subtitle", "Content", 3)
        );
        verify(notificationRepository).save(any(Notification.class));
    }
}
