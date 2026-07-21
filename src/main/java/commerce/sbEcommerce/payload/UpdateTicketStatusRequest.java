package commerce.sbEcommerce.payload;

import commerce.sbEcommerce.model.TicketStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTicketStatusRequest {

    @NotNull(message = "Ticket status is required")
    private TicketStatus status;
}
