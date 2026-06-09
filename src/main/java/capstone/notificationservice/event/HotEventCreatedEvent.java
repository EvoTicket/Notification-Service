package capstone.notificationservice.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HotEventCreatedEvent {
    private Long eventId;
    private String eventName;
    private Long totalSeats;
    private String thumbnailImage;
}
