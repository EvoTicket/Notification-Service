package capstone.notificationservice.entity;

import capstone.notificationservice.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "notifications")
public class Notification {

    @Id
    private String id;

    @Indexed
    private Long userId;

    private String title;

    private String message;

    @Indexed
    private NotificationType type;

    @Builder.Default
    private boolean isRead = false;

    @Indexed
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime readAt;

    private String imageUrl;
}
