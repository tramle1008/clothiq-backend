package commerce.sbEcommerce.controller;

import commerce.sbEcommerce.config.AppConstants;
import commerce.sbEcommerce.payload.CreateTicketRequest;
import commerce.sbEcommerce.payload.ReplyTicketRequest;
import commerce.sbEcommerce.payload.TicketDetailResponse;
import commerce.sbEcommerce.payload.TicketPageResponse;
import commerce.sbEcommerce.payload.TicketSummaryResponse;
import commerce.sbEcommerce.payload.UpdateTicketStatusRequest;
import commerce.sbEcommerce.service.TicketService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @PostMapping(value = "/tickets", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<TicketDetailResponse> createTicket(@Valid @ModelAttribute CreateTicketRequest request) throws IOException {
        return ResponseEntity.status(HttpStatus.CREATED).body(ticketService.createTicket(request));
    }

    @GetMapping("/tickets")
    public ResponseEntity<List<TicketSummaryResponse>> getMyTickets() {
        return ResponseEntity.ok(ticketService.getMyTickets());
    }

    @GetMapping("/tickets/{ticketId}")
    public ResponseEntity<TicketDetailResponse> getTicketDetail(@PathVariable Long ticketId) {
        return ResponseEntity.ok(ticketService.getMyTicketDetail(ticketId));
    }

    @PostMapping(value = "/tickets/{ticketId}/messages", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<TicketDetailResponse> replyTicket(@PathVariable Long ticketId,
                                                            @Valid @ModelAttribute ReplyTicketRequest request) throws IOException {
        return ResponseEntity.ok(ticketService.replyToMyTicket(ticketId, request));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/tickets")
    public ResponseEntity<TicketPageResponse> getAllTickets(
            @RequestParam(name = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
            @RequestParam(name = "pageSize", defaultValue = AppConstants.PAGE_SIZE_TICKET, required = false) Integer pageSize,
            @RequestParam(name = "sortBy", defaultValue = AppConstants.SORT_BY_TICKET, required = false) String sortBy,
            @RequestParam(name = "sortOrder", defaultValue = AppConstants.SORT_ORDER_TICKET, required = false) String sortOrder
    ) {
        return ResponseEntity.ok(ticketService.getAllTickets(pageNumber, pageSize, sortBy, sortOrder));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/tickets/{ticketId}")
    public ResponseEntity<TicketDetailResponse> getAdminTicketDetail(@PathVariable Long ticketId) {
        return ResponseEntity.ok(ticketService.getAdminTicketDetail(ticketId));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(value = "/admin/tickets/{ticketId}/messages", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<TicketDetailResponse> replyTicketAsAdmin(@PathVariable Long ticketId,
                                                                   @Valid @ModelAttribute ReplyTicketRequest request) throws IOException {
        return ResponseEntity.ok(ticketService.replyToAnyTicket(ticketId, request));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/admin/tickets/{ticketId}/status")
    public ResponseEntity<TicketDetailResponse> updateStatus(@PathVariable Long ticketId,
                                                             @Valid @RequestBody UpdateTicketStatusRequest request) {
        return ResponseEntity.ok(ticketService.updateTicketStatus(ticketId, request));
    }

    @PutMapping("/tickets/{ticketId}/close")
    public ResponseEntity<TicketDetailResponse> closeTicket(@PathVariable Long ticketId) {
        return ResponseEntity.ok(ticketService.closeTicketByCustomer(ticketId));
    }
}