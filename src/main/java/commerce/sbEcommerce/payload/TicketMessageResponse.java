package commerce.sbEcommerce.payload;

import commerce.sbEcommerce.model.TicketSenderType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketMessageResponse {
    private Long id;
    private Long senderId;
    private String senderName;
    private TicketSenderType senderType;
    private String content;
    private LocalDateTime createdAt;
    private List<String> imageUrls;
}
