package org.example.be_sp.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.be_sp.entity.NhanVien;
import org.example.be_sp.entity.NotificationPreference;
import org.example.be_sp.exception.ApiException;
import org.example.be_sp.repository.NhanVienRepository;
import org.example.be_sp.repository.NotificationPreferenceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationPreferenceService {
    
    private final NotificationPreferenceRepository preferenceRepository;
    private final NhanVienRepository nhanVienRepository;
    
    /**
     * Get or create user notification preferences
     */
    public NotificationPreference getOrCreatePreferences(Integer userId) {
        return preferenceRepository.findByUserIdAndDeleted(userId, false)
                .orElseGet(() -> createDefaultPreferences(userId));
    }
    
    /**
     * Create default preferences for a user
     */
    @Transactional
    private NotificationPreference createDefaultPreferences(Integer userId) {
        NhanVien user = nhanVienRepository.findById(userId)
                .orElseThrow(() -> new ApiException("User not found", "404"));
        
        NotificationPreference pref = new NotificationPreference();
        pref.setUser(user);
        pref.setEnableOrderNotifications(true);
        pref.setEnableStockAlerts(true);
        pref.setEnableSystemNotices(true);
        pref.setEnableChatMessages(true);
        pref.setEnableWebPush(true);
        pref.setEnableEmail(false);
        pref.setEnableQuietHours(false);
        pref.setQuietStartHour(22);
        pref.setQuietEndHour(7);
        pref.setDeleted(false);
        
        return preferenceRepository.save(pref);
    }
    
    /**
     * Update user preferences
     */
    @Transactional
    public NotificationPreference updatePreferences(Integer userId, NotificationPreference request) {
        NotificationPreference pref = getOrCreatePreferences(userId);
        
        if (request.getEnableOrderNotifications() != null) {
            pref.setEnableOrderNotifications(request.getEnableOrderNotifications());
        }
        if (request.getEnableStockAlerts() != null) {
            pref.setEnableStockAlerts(request.getEnableStockAlerts());
        }
        if (request.getEnableSystemNotices() != null) {
            pref.setEnableSystemNotices(request.getEnableSystemNotices());
        }
        if (request.getEnableChatMessages() != null) {
            pref.setEnableChatMessages(request.getEnableChatMessages());
        }
        if (request.getEnableWebPush() != null) {
            pref.setEnableWebPush(request.getEnableWebPush());
        }
        if (request.getEnableEmail() != null) {
            pref.setEnableEmail(request.getEnableEmail());
        }
        if (request.getEnableQuietHours() != null) {
            pref.setEnableQuietHours(request.getEnableQuietHours());
        }
        if (request.getQuietStartHour() != null) {
            pref.setQuietStartHour(request.getQuietStartHour());
        }
        if (request.getQuietEndHour() != null) {
            pref.setQuietEndHour(request.getQuietEndHour());
        }
        
        return preferenceRepository.save(pref);
    }
    
    /**
     * Check if notifications should be sent to this user based on preferences
     * @param userId User ID
     * @param notificationType Type of notification (order, stock, system, chat)
     * @return true if notification should be sent
     */
    public boolean shouldSendNotification(Integer userId, String notificationType) {
        try {
            NotificationPreference pref = preferenceRepository.findByUserIdAndDeleted(userId, false)
                    .orElse(null);
            
            // If no preferences found, send all notifications (default behavior)
            if (pref == null) {
                return true;
            }
            
            // Check quiet hours
            if (pref.getEnableQuietHours() && isInQuietHours(pref)) {
                return false;
            }
            
            // Check notification type preferences
            switch (notificationType.toLowerCase()) {
                case "order":
                    return pref.getEnableOrderNotifications();
                case "stock":
                    return pref.getEnableStockAlerts();
                case "system":
                    return pref.getEnableSystemNotices();
                case "chat":
                    return pref.getEnableChatMessages();
                default:
                    return true;  // Send unknown types by default
            }
        } catch (Exception e) {
            log.error("Error checking notification preferences: {}", e.getMessage());
            return true;  // Fail open - send notification on error
        }
    }
    
    /**
     * Check if current time is in user's quiet hours
     */
    private boolean isInQuietHours(NotificationPreference pref) {
        LocalTime now = LocalTime.now();
        int currentHour = now.getHour();
        int startHour = pref.getQuietStartHour();
        int endHour = pref.getQuietEndHour();
        
        // Handle overnight quiet hours (e.g., 22:00 to 07:00)
        if (startHour > endHour) {
            return currentHour >= startHour || currentHour < endHour;
        } else {
            return currentHour >= startHour && currentHour < endHour;
        }
    }
}
