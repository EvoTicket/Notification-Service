package capstone.notificationservice.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketTransferredEvent {
    private String ticketCode;
    private String eventName;
    private String ticketTypeName;
    private Long fromUserId;
    private Long toUserId;
}
