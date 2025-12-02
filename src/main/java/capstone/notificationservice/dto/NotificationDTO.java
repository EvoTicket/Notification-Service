package capstone.notificationservice.dto;

import capstone.notificationservice.entity.Notification;
import capstone.notificationservice.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Optional;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationDTO {

    private String id;
    private Long userId;
    private String title;
    private String message;
    private NotificationType type;
    private boolean isRead;
    private LocalDateTime createdAt;
    private LocalDateTime readAt;
    private String imageUrl;

    public static NotificationDTO fromEntity(Notification notification) {
        return NotificationDTO.builder()
                .id(notification.getId())
                .userId(notification.getUserId())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .type(notification.getType())
                .isRead(notification.isRead())
                .createdAt(notification.getCreatedAt().atZone(ZoneId.systemDefault()).toLocalDateTime())
                .readAt(Optional.ofNullable(notification.getReadAt())
                        .map(t -> t.atZone(ZoneId.systemDefault()).toLocalDateTime())
                        .orElse(null))
                .imageUrl(notification.getImageUrl())
                .build();
    }
}
