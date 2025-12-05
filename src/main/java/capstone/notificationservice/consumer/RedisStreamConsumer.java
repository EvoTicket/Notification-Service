package capstone.notificationservice.consumer;

import capstone.notificationservice.enums.NotificationType;
import capstone.notificationservice.event.OtpEvent;
import capstone.notificationservice.event.WelcomeEvent;
import capstone.notificationservice.exception.AppException;
import capstone.notificationservice.exception.ErrorCode;
import capstone.notificationservice.security.JwtUtil;
import capstone.notificationservice.service.EmailService;
import capstone.notificationservice.service.NotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.data.redis.stream.Subscription;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisStreamConsumer implements StreamListener<String, MapRecord<String, String, String>> {

    private final StreamMessageListenerContainer<String, MapRecord<String, String, String>> listenerContainer;
    private final RedisTemplate<String, Object> redisTemplate;
    private final EmailService emailService;
    private final NotificationService notificationService;
    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${app.logo}")
    private String logo;
    private static final List<String> LIST_STREAM_KEY = List.of(
            "forgot-password-otp",
            "welcome-signup");
    private static final String CONSUMER_GROUP = "notification-service-group";
    private static final String CONSUMER_NAME = "notification-1";

    private final List<Subscription> subscriptions = new ArrayList<>();

    @PostConstruct
    public void init() {
        LIST_STREAM_KEY.forEach(streamKey -> {
            try {
                redisTemplate.opsForStream().createGroup(streamKey, CONSUMER_GROUP);
                log.info("Created consumer group '{}' for stream '{}'", CONSUMER_GROUP, streamKey);
            } catch (Exception e) {
                log.info("Consumer group '{}' already exists for stream '{}'", CONSUMER_GROUP, streamKey);
            }
        });

        LIST_STREAM_KEY.forEach(streamKey -> {

            Subscription sub = listenerContainer.receive(
                    Consumer.from(CONSUMER_GROUP, CONSUMER_NAME),
                    StreamOffset.create(streamKey, ReadOffset.lastConsumed()),
                    this);

            subscriptions.add(sub);

            log.info("Subscribed consumer '{}' to stream '{}'", CONSUMER_NAME, streamKey);
        });

        listenerContainer.start();
        log.info("Redis Stream consumer started for all streams: {}", LIST_STREAM_KEY);
    }

    @Override
    public void onMessage(MapRecord<String, String, String> message) {
        try {
            String messageId = message.getId().getValue();
            Map<String, String> body = message.getValue();

            String payload = body.get("payload");

            log.info("Received message [ID: {}]: {}", messageId, payload);

            filterMessage(payload, message.getRequiredStream());

            redisTemplate.opsForStream()
                    .acknowledge(message.getRequiredStream(), CONSUMER_GROUP, messageId);

            log.info("Message acknowledged: {}", messageId);

        } catch (Exception e) {
            log.error("Error processing message", e);
        }
    }

    private void filterMessage(String payload, String stream) {
        log.info("Processing: {}", payload);

        switch (stream) {
            case "forgot-password-otp" -> handleForgotPasswordOtp(payload);
            case "welcome-signup" -> handleWelcomeSignup(payload);
            default -> throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR, "Unknown stream: " + stream);
        }
    }

    private void handleForgotPasswordOtp(String payload) {
        try {
            OtpEvent otpEvent = objectMapper.readValue(payload, OtpEvent.class);
            log.info("Processing OTP event for email: {}", otpEvent.getEmail());

            emailService.sendOtpEmail(otpEvent.getEmail(), otpEvent.getOtpCode());
            log.info("OTP email sent successfully for: {}", otpEvent.getEmail());

        } catch (Exception e) {
            log.error("Error processing OTP event", e);
        }
    }

    private void handleWelcomeSignup(String payload) {
        try {
            WelcomeEvent welcomeEvent = objectMapper.readValue(payload, WelcomeEvent.class);
            log.info("Processing Welcome event for email: {}", welcomeEvent.getEmail());

            emailService.sendWelcomeEmail(
                    welcomeEvent.getEmail(),
                    welcomeEvent.getFullName(),
                    welcomeEvent.getUsername());
            log.info("Welcome email sent successfully for: {}", welcomeEvent.getEmail());

            notificationService.createAndSendNotification(
                    welcomeEvent.getUserId(),
                    "Chào mừng đến với EvoTicket!",
                    "Xin chào " + welcomeEvent.getFullName()
                            + "! Tài khoản của bạn đã được tạo thành công. Chúc bạn có trải nghiệm tuyệt vời!",
                    NotificationType.WELCOME,
                    logo
            );

        } catch (Exception e) {
            log.error("Error processing Welcome event", e);
        }
    }

    @PreDestroy
    public void destroy() {
        subscriptions.forEach(Subscription::cancel);
        listenerContainer.stop();
        log.info("Redis Stream consumer stopped");
    }
}