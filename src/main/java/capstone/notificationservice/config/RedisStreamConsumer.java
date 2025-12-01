package capstone.notificationservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.data.redis.stream.Subscription;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisStreamConsumer implements StreamListener<String, MapRecord<String, String, String>> {

    private final StreamMessageListenerContainer<String, MapRecord<String, String, String>> listenerContainer;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String STREAM_KEY = "order-events";
    private static final String CONSUMER_GROUP = "order-service-group";
    private static final String CONSUMER_NAME = "consumer-1";

    private Subscription subscription;

    @PostConstruct
    public void init() {
        // Tạo consumer group nếu chưa tồn tại
        try {
            redisTemplate.opsForStream()
                    .createGroup(STREAM_KEY, CONSUMER_GROUP);
            log.info("Created consumer group '{}' for stream '{}'", CONSUMER_GROUP, STREAM_KEY);
        } catch (Exception e) {
            log.info("Consumer group '{}' already exists", CONSUMER_GROUP);
        }

        // Đăng ký listener
        subscription = listenerContainer.receive(
                Consumer.from(CONSUMER_GROUP, CONSUMER_NAME),
                StreamOffset.create(STREAM_KEY, ReadOffset.lastConsumed()),
                this
        );

        listenerContainer.start();
        log.info("Redis Stream consumer started for stream '{}'", STREAM_KEY);
    }

    @Override
    public void onMessage(MapRecord<String, String, String> message) {
        try {
            String messageId = message.getId().getValue();
            Map<String, String> body = message.getValue();

            String payload = body.get("payload");
            String timestamp = body.get("timestamp");

            log.info("Received message [ID: {}]: {}", messageId, payload);

            // Xử lý message ở đây
            processMessage(payload);

            // Acknowledge message (xác nhận đã xử lý thành công)
            redisTemplate.opsForStream()
                    .acknowledge(STREAM_KEY, CONSUMER_GROUP, messageId);

            log.info("Message acknowledged: {}", messageId);

        } catch (Exception e) {
            log.error("Error processing message", e);
            // Có thể implement retry logic hoặc dead letter queue ở đây
        }
    }

    private void processMessage(String payload) {
        // Business logic xử lý message
        log.info("Processing: {}", payload);

        // Ví dụ: parse JSON và xử lý
        try {
            // OrderEvent order = objectMapper.readValue(payload, OrderEvent.class);
            // ... xử lý order
        } catch (Exception e) {
            log.error("Error parsing message payload", e);
        }
    }

    @PreDestroy
    public void destroy() {
        if (subscription != null) {
            subscription.cancel();
        }
        if (listenerContainer != null) {
            listenerContainer.stop();
        }
        log.info("Redis Stream consumer stopped");
    }
}