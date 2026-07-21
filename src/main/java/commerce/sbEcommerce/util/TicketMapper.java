package commerce.sbEcommerce.util;

import commerce.sbEcommerce.payload.TicketDetailResponse;
import commerce.sbEcommerce.payload.TicketMessageResponse;
import commerce.sbEcommerce.payload.TicketPageResponse;
import commerce.sbEcommerce.payload.TicketSummaryResponse;
import commerce.sbEcommerce.model.Ticket;
import commerce.sbEcommerce.model.TicketMessage;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TicketMapper {

    public TicketSummaryResponse toSummaryResponse(Ticket ticket) {
        Long customerId = ticket.getCustomer() != null ? ticket.getCustomer().getUserId() : null;
        String customerName = ticket.getCustomer() != null ? ticket.getCustomer().getUserName() : null;
        return new TicketSummaryResponse(
                ticket.getId(),
                ticket.getTitle(),
                ticket.getStatus(),
                ticket.getCreatedAt(),
                ticket.getUpdatedAt(),
                ticket.getClosedAt(),
                customerId,
                customerName
        );
    }

    public TicketMessageResponse toMessageResponse(TicketMessage message) {
        Long senderId = message.getSender() != null ? message.getSender().getUserId() : null;
        String senderName = message.getSender() != null ? message.getSender().getUserName() : null;
        List<String> imageUrls = message.getImages().stream()
                .map(image -> image.getImageUrl())
                .toList();
        return new TicketMessageResponse(
                message.getId(),
                senderId,
                senderName,
                message.getSenderType(),
                message.getContent(),
                message.getCreatedAt(),
                imageUrls
        );
    }

    public TicketDetailResponse toDetailResponse(Ticket ticket, List<TicketMessage> messages) {
        return new TicketDetailResponse(
                ticket.getId(),
                ticket.getTitle(),
                ticket.getStatus(),
                ticket.getCreatedAt(),
                ticket.getUpdatedAt(),
                ticket.getClosedAt(),
                ticket.getCustomer() != null ? ticket.getCustomer().getUserId() : null,
                ticket.getCustomer() != null ? ticket.getCustomer().getUserName() : null,
                messages.stream().map(this::toMessageResponse).toList()
        );
    }

    public TicketPageResponse toPageResponse(Page<Ticket> page) {
        return new TicketPageResponse(
                page.getContent().stream().map(this::toSummaryResponse).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }
}
