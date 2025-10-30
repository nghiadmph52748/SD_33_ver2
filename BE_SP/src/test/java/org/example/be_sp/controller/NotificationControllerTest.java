package org.example.be_sp.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.be_sp.model.response.NotificationResponse;
import org.example.be_sp.service.NotificationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NotificationController.class)
@AutoConfigureMockMvc(addFilters = false) // Disable security for testing
@DisplayName("Notification REST API Tests")
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private NotificationService notificationService;

    @Test
    @DisplayName("GET /api/message/list - Should return list of notifications")
    void testGetNotifications_Success() throws Exception {
        // Arrange
        NotificationResponse notification = NotificationResponse.builder()
                .id(1)
                .type("notice")
                .title("Test Notification")
                .content("Test content")
                .status(0)
                .build();
        
        List<NotificationResponse> notifications = Arrays.asList(notification);
        when(notificationService.getNotificationsForUser(anyInt())).thenReturn(notifications);

        // Act & Assert
        mockMvc.perform(post("/api/message/list")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].title").value("Test Notification"));

        verify(notificationService).getNotificationsForUser(anyInt());
    }

    @Test
    @DisplayName("POST /api/message/read - Should mark notifications as read")
    void testMarkAsRead_Success() throws Exception {
        // Arrange
        Map<String, List<Integer>> request = new HashMap<>();
        request.put("ids", Arrays.asList(1, 2, 3));
        
        doNothing().when(notificationService).markAsRead(any());

        // Act & Assert
        mockMvc.perform(post("/api/message/read")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(true))
                .andExpect(jsonPath("$.message").value("Đánh dấu đã đọc thành công"));

        verify(notificationService).markAsRead(Arrays.asList(1, 2, 3));
    }

    @Test
    @DisplayName("POST /api/message/read - Should reject empty ID list")
    void testMarkAsRead_EmptyIds() throws Exception {
        // Arrange
        Map<String, List<Integer>> request = new HashMap<>();
        request.put("ids", Arrays.asList());

        // Act & Assert
        mockMvc.perform(post("/api/message/read")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Danh sách ID không được rỗng"));

        verify(notificationService, never()).markAsRead(any());
    }

    @Test
    @DisplayName("GET /api/message/unread-count - Should return unread count")
    void testGetUnreadCount_Success() throws Exception {
        // Arrange
        when(notificationService.getUnreadCount(anyInt())).thenReturn(5L);

        // Act & Assert
        mockMvc.perform(get("/api/message/unread-count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(5));

        verify(notificationService).getUnreadCount(anyInt());
    }

    @Test
    @DisplayName("POST /api/message/clear-all - Should clear all notifications")
    void testClearAll_Success() throws Exception {
        // Arrange
        doNothing().when(notificationService).clearAllForUser(anyInt());

        // Act & Assert
        mockMvc.perform(post("/api/message/clear-all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(true))
                .andExpect(jsonPath("$.message").value("Xóa tất cả thông báo thành công"));

        verify(notificationService).clearAllForUser(anyInt());
    }

    @Test
    @DisplayName("POST /api/message/test-notification - Should create test notification")
    void testCreateTestNotification_Success() throws Exception {
        // Arrange
        Map<String, Object> request = new HashMap<>();
        request.put("type", "notice");
        request.put("title", "Test");
        request.put("content", "Test content");
        
        org.example.be_sp.entity.Notification notification = new org.example.be_sp.entity.Notification();
        notification.setId(1);
        notification.setTitle("Test");
        
        when(notificationService.createNotification(anyInt(), any(), any(), any(), any(), anyInt()))
                .thenReturn(notification);

        // Act & Assert
        mockMvc.perform(post("/api/message/test-notification")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Tạo thông báo thử nghiệm thành công"));

        verify(notificationService).createNotification(anyInt(), any(), any(), any(), any(), anyInt());
    }

    @Test
    @DisplayName("POST /api/message/test-notification - Should accept custom user ID")
    void testCreateTestNotification_WithCustomUserId() throws Exception {
        // Arrange
        Map<String, Object> request = new HashMap<>();
        request.put("userId", 5);
        request.put("type", "message");
        request.put("title", "Custom Test");
        
        org.example.be_sp.entity.Notification notification = new org.example.be_sp.entity.Notification();
        notification.setId(1);
        
        when(notificationService.createNotification(eq(5), any(), any(), any(), any(), anyInt()))
                .thenReturn(notification);

        // Act & Assert
        mockMvc.perform(post("/api/message/test-notification")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(notificationService).createNotification(eq(5), any(), any(), any(), any(), anyInt());
    }
}
