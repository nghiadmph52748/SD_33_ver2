package org.example.be_sp.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.be_sp.entity.Notification;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {
    private Integer id;
    private String type;
    private String title;
    private String subTitle;
    private String avatar;
    private String content;
    private String time;
    private Integer status;
    private Integer messageType;
    
    public static NotificationResponse fromEntity(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .type(notification.getType())
                .title(notification.getTitle())
                .subTitle(notification.getSubTitle())
                .avatar(notification.getAvatar())
                .content(notification.getContent())
                .time(formatTime(notification.getCreatedAt()))
                .status(notification.getStatus())
                .messageType(notification.getMessageType())
                .build();
    }
    
    private static String formatTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }
        
        LocalDateTime now = LocalDateTime.now();
        long daysDiff = java.time.Duration.between(dateTime, now).toDays();
        
        if (daysDiff == 0) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
            return "Hôm nay " + dateTime.format(formatter);
        } else if (daysDiff == 1) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
            return "Hôm qua " + dateTime.format(formatter);
        } else {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            return dateTime.format(formatter);
        }
    }
}
