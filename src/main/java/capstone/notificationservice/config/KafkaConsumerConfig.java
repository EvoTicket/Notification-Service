package capstone.notificationservice.config;

import capstone.notificationservice.event.OtpEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConsumerConfig {

    private Map<String, Object> commonProps() {
        Map<String, Object> props = new HashMap<>();

        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "notification-group");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                org.springframework.kafka.support.serializer.ErrorHandlingDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                org.springframework.kafka.support.serializer.ErrorHandlingDeserializer.class);

        props.put("spring.deserializer.key.delegate.class",
                "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("spring.deserializer.value.delegate.class",
                "org.springframework.kafka.support.serializer.JsonDeserializer");

        props.put("spring.json.trusted.packages", "*");
        props.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);

        return props;
    }

    private <T> ConcurrentKafkaListenerContainerFactory<String, T> createFactory(Class<T> clazz) {
        Map<String, Object> props = commonProps();
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, clazz.getName());

        DefaultKafkaConsumerFactory<String, T> consumerFactory =
                new DefaultKafkaConsumerFactory<>(
                        props,
                        new StringDeserializer(),
                        new JsonDeserializer<>(clazz, false)
                );

        ConcurrentKafkaListenerContainerFactory<String, T> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        return factory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, OtpEvent> otpListenerFactory() {
        return createFactory(OtpEvent.class);
    }
}