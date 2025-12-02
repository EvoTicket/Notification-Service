package capstone.notificationservice.service;

import capstone.notificationservice.dto.NotificationDTO;
import capstone.notificationservice.dto.NotificationPageResponse;
import capstone.notificationservice.entity.Notification;
import capstone.notificationservice.enums.NotificationType;
import capstone.notificationservice.exception.AppException;
import capstone.notificationservice.exception.ErrorCode;
import capstone.notificationservice.repository.NotificationRepository;
import capstone.notificationservice.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final JwtUtil jwtUtil;
    public void sendRealtimeNotification(Long userId, NotificationDTO notification) {
        try {
            messagingTemplate.convertAndSendToUser(
                    userId.toString(),
                    "/queue/notifications",
                    notification);
            log.info("Realtime notification sent to user: {}", userId);
        } catch (Exception e) {
            log.error("Failed to send realtime notification to user: {}", userId, e);
        }
    }

    public void createAndSendNotification(Long userId, String title, String message,
                                          NotificationType type, String imageUrl) {

        Notification notification = Notification.builder()
                .userId(userId)
                .title(title)
                .message(message)
                .type(type)
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .imageUrl(imageUrl)
                .build();

        notificationRepository.save(notification);

        NotificationDTO dto = NotificationDTO.fromEntity(notification);

        sendRealtimeNotification(userId, dto);
    }

    public NotificationPageResponse getNotifications(Pageable pageable) {
        Page<Notification> page = notificationRepository.findByUserIdOrderByCreatedAtDesc(jwtUtil.getDataFromAuth().userId(), pageable);

        List<NotificationDTO> content = page.getContent().stream()
                .map(NotificationDTO::fromEntity)
                .toList();

        return NotificationPageResponse.builder()
                .content(content)
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .currentPage(page.getNumber())
                .pageSize(page.getSize())
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .build();
    }

    public NotificationDTO markAsRead(String notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Notification not found"));

        if (!notification.getUserId().equals(jwtUtil.getDataFromAuth().userId())) {
            throw new AppException(ErrorCode.FORBIDDEN);
        }

        if (!notification.isRead()) {
            notification.setRead(true);
            notification.setReadAt(LocalDateTime.now());
            notification = notificationRepository.save(notification);
            log.info("Notification {} marked as read", notificationId);
        }

        return NotificationDTO.fromEntity(notification);
    }

    public void markAllAsRead() {
        List<Notification> unreadNotifications = notificationRepository.findByUserIdAndIsReadFalse(jwtUtil.getDataFromAuth().userId());

        LocalDateTime now = LocalDateTime.now();
        unreadNotifications.forEach(notification -> {
            notification.setRead(true);
            notification.setReadAt(now);
        });

        notificationRepository.saveAll(unreadNotifications);
    }

    public long getUnreadCount() {
        return notificationRepository.countByUserIdAndIsReadFalse(jwtUtil.getDataFromAuth().userId());
    }
}
