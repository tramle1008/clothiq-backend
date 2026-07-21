package commerce.sbEcommerce.service;

import commerce.sbEcommerce.payload.CreateTicketRequest;
import commerce.sbEcommerce.payload.ReplyTicketRequest;
import commerce.sbEcommerce.payload.TicketDetailResponse;
import commerce.sbEcommerce.payload.TicketPageResponse;
import commerce.sbEcommerce.payload.TicketSummaryResponse;
import commerce.sbEcommerce.payload.UpdateTicketStatusRequest;
import jakarta.validation.Valid;

import java.io.IOException;
import java.util.List;

public interface TicketService {
    TicketDetailResponse createTicket(CreateTicketRequest request) throws IOException;

    List<TicketSummaryResponse> getMyTickets();

    TicketDetailResponse getMyTicketDetail(Long ticketId);

    TicketDetailResponse replyToMyTicket(Long ticketId, ReplyTicketRequest request) throws IOException;

    TicketPageResponse getAllTickets(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder);

    TicketDetailResponse getAdminTicketDetail(Long ticketId);

    TicketDetailResponse replyToAnyTicket(Long ticketId, ReplyTicketRequest request) throws IOException;

    TicketDetailResponse updateTicketStatus(Long ticketId, UpdateTicketStatusRequest request);

    TicketDetailResponse closeTicketByCustomer(Long ticketId);
}