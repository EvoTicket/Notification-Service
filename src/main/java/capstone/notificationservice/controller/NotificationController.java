package capstone.notificationservice.controller;

import capstone.notificationservice.dto.NotificationDTO;
import capstone.notificationservice.dto.NotificationPageResponse;
import capstone.notificationservice.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "Notification management APIs")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    @Operation(summary = "Get user notifications", description = "Get paginated list of notifications for a user")
    public ResponseEntity<NotificationPageResponse> getNotifications(
            @Parameter(description = "User ID", required = true) @RequestParam String userId,
            @Parameter(description = "Page number (1-indexed)") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {
        log.info("Getting notifications for user: {}, page: {}, size: {}", userId, page, size);
        Pageable pageable = PageRequest.of(page - 1, size);
        NotificationPageResponse response = notificationService.getNotifications(userId, pageable);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/read")
    @Operation(summary = "Mark notification as read", description = "Mark a single notification as read")
    public ResponseEntity<NotificationDTO> markAsRead(
            @Parameter(description = "Notification ID", required = true) @PathVariable String id,
            @Parameter(description = "User ID", required = true) @RequestParam String userId) {
        log.info("Marking notification {} as read for user: {}", id, userId);
        NotificationDTO notification = notificationService.markAsRead(id, userId);
        return ResponseEntity.ok(notification);
    }

    @PutMapping("/read-all")
    @Operation(summary = "Mark all as read", description = "Mark all notifications as read for a user")
    public ResponseEntity<Map<String, String>> markAllAsRead(
            @Parameter(description = "User ID", required = true) @RequestParam String userId) {
        log.info("Marking all notifications as read for user: {}", userId);
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "All notifications marked as read"));
    }

    @GetMapping("/unread-count")
    @Operation(summary = "Get unread count", description = "Get count of unread notifications for a user")
    public ResponseEntity<Map<String, Long>> getUnreadCount(
            @Parameter(description = "User ID", required = true) @RequestParam String userId) {
        log.info("Getting unread count for user: {}", userId);
        long count = notificationService.getUnreadCount(userId);
        return ResponseEntity.ok(Map.of("unreadCount", count));
    }
}
