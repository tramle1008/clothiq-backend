package commerce.sbEcommerce.payload;

import commerce.sbEcommerce.model.TicketStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketSummaryResponse {
    private Long id;
    private String title;
    private TicketStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime closedAt;
    private Long customerId;
    private String customerName;
}
