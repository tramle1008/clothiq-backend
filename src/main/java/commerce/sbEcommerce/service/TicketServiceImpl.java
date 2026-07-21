package commerce.sbEcommerce.service;

import commerce.sbEcommerce.exceptioons.ResourceNotFoundException;
import commerce.sbEcommerce.model.User;
import commerce.sbEcommerce.repository.UserRepository;
import commerce.sbEcommerce.payload.CreateTicketRequest;
import commerce.sbEcommerce.payload.ReplyTicketRequest;
import commerce.sbEcommerce.payload.TicketDetailResponse;
import commerce.sbEcommerce.payload.TicketPageResponse;
import commerce.sbEcommerce.payload.TicketSummaryResponse;
import commerce.sbEcommerce.payload.UpdateTicketStatusRequest;
import commerce.sbEcommerce.model.Ticket;
import commerce.sbEcommerce.model.TicketMessage;
import commerce.sbEcommerce.model.TicketMessageImage;
import commerce.sbEcommerce.model.TicketSenderType;
import commerce.sbEcommerce.model.TicketStatus;
import commerce.sbEcommerce.exceptioons.ConflictException;
import commerce.sbEcommerce.exceptioons.ForbiddenException;
import commerce.sbEcommerce.util.TicketMapper;
import commerce.sbEcommerce.repository.TicketMessageRepository;
import commerce.sbEcommerce.repository.TicketRepository;
import commerce.sbEcommerce.util.AuthUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class TicketServiceImpl implements TicketService {

    private final TicketRepository ticketRepository;
    private final TicketMessageRepository ticketMessageRepository;
    private final UserRepository userRepository;
    private final FileService fileService;
    private final AuthUtil authUtil;
    private final TicketMapper ticketMapper;

    @Value("${project.image}")
    private String imagePath;

    @Value("${image.base.url}")
    private String imageBaseUrl;

    public TicketServiceImpl(TicketRepository ticketRepository,
                             TicketMessageRepository ticketMessageRepository,
                             UserRepository userRepository,
                             FileService fileService,
                             AuthUtil authUtil,
                             TicketMapper ticketMapper) {
        this.ticketRepository = ticketRepository;
        this.ticketMessageRepository = ticketMessageRepository;
        this.userRepository = userRepository;
        this.fileService = fileService;
        this.authUtil = authUtil;
        this.ticketMapper = ticketMapper;
    }

    @Override
    @Transactional
    public TicketDetailResponse createTicket(CreateTicketRequest request) throws IOException {
        User customer = currentUser();

        Ticket ticket = new Ticket();
        ticket.setCustomer(customer);
        ticket.setTitle(request.getTitle().trim());
        ticket.setStatus(TicketStatus.OPEN);

        TicketMessage message = buildMessage(customer, TicketSenderType.CUSTOMER, request.getContent(), request.getImages());
        ticket.addMessage(message);

        Ticket saved = ticketRepository.save(ticket);
        return ticketMapper.toDetailResponse(saved, saved.getMessages());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TicketSummaryResponse> getMyTickets() {
        Long currentUserId = authUtil.getCurrentUserId();
        return ticketRepository.findByCustomerUserIdOrderByCreatedAtDesc(currentUserId)
                .stream()
                .map(ticketMapper::toSummaryResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public TicketDetailResponse getMyTicketDetail(Long ticketId) {
        Ticket ticket = loadTicketForCurrentCustomer(ticketId);
        List<TicketMessage> messages = ticketMessageRepository.findByTicketIdOrderByCreatedAtAsc(ticketId);
        return ticketMapper.toDetailResponse(ticket, messages);
    }

    @Override
    @Transactional
    public TicketDetailResponse replyToMyTicket(Long ticketId, ReplyTicketRequest request) throws IOException {
        Ticket ticket = loadTicketForCurrentCustomer(ticketId);
        if (ticket.getStatus() == TicketStatus.CLOSED) {
            throw new ConflictException("Cannot reply to a closed ticket.");
        }

        User customer = currentUser();
        TicketMessage message = buildMessage(customer, TicketSenderType.CUSTOMER, request.getContent(), request.getImages());
        ticket.addMessage(message);

        ticketRepository.save(ticket);
        List<TicketMessage> messages = ticketMessageRepository.findByTicketIdOrderByCreatedAtAsc(ticketId);
        Ticket refreshed = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", "ticketId", ticketId));
        return ticketMapper.toDetailResponse(refreshed, messages);
    }

    @Override
    @Transactional(readOnly = true)
    public TicketPageResponse getAllTickets(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize, buildSort(sortBy, sortOrder));
        return ticketMapper.toPageResponse(ticketRepository.findAll(pageable));
    }

    @Override
    @Transactional(readOnly = true)
    public TicketDetailResponse getAdminTicketDetail(Long ticketId) {
        Ticket ticket = loadTicket(ticketId);
        List<TicketMessage> messages = ticketMessageRepository.findByTicketIdOrderByCreatedAtAsc(ticketId);
        return ticketMapper.toDetailResponse(ticket, messages);
    }

    @Override
    @Transactional
    public TicketDetailResponse replyToAnyTicket(Long ticketId, ReplyTicketRequest request) throws IOException {
        Ticket ticket = loadTicket(ticketId);
        User admin = currentUser();
        TicketMessage message = buildMessage(admin, TicketSenderType.ADMIN, request.getContent(), request.getImages());
        ticket.addMessage(message);
        ticketRepository.save(ticket);

        List<TicketMessage> messages = ticketMessageRepository.findByTicketIdOrderByCreatedAtAsc(ticketId);
        Ticket refreshed = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", "ticketId", ticketId));
        return ticketMapper.toDetailResponse(refreshed, messages);
    }

    @Override
    @Transactional
    public TicketDetailResponse updateTicketStatus(Long ticketId, UpdateTicketStatusRequest request) {
        Ticket ticket = loadTicket(ticketId);
        TicketStatus newStatus = request.getStatus();

        if (ticket.getStatus() == TicketStatus.CLOSED) {
            throw new ConflictException("Closed ticket cannot be updated.");
        }

        ticket.setStatus(newStatus);
        if (newStatus == TicketStatus.CLOSED) {
            ticket.setClosedAt(LocalDateTime.now());
        } else if (ticket.getClosedAt() != null) {
            ticket.setClosedAt(null);
        }

        Ticket saved = ticketRepository.save(ticket);
        List<TicketMessage> messages = ticketMessageRepository.findByTicketIdOrderByCreatedAtAsc(ticketId);
        Ticket refreshed = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", "ticketId", ticketId));
        return ticketMapper.toDetailResponse(refreshed, messages);
    }

    @Override
    @Transactional
    public TicketDetailResponse closeTicketByCustomer(Long ticketId) {
        Ticket ticket = loadTicketForCurrentCustomer(ticketId);

        if (ticket.getStatus() == TicketStatus.CLOSED) {
            throw new ConflictException("Ticket is already closed.");
        }

        ticket.setStatus(TicketStatus.CLOSED);
        ticket.setClosedAt(LocalDateTime.now());

        Ticket saved = ticketRepository.save(ticket);
        List<TicketMessage> messages = ticketMessageRepository.findByTicketIdOrderByCreatedAtAsc(ticketId);
        Ticket refreshed = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", "ticketId", ticketId));
        return ticketMapper.toDetailResponse(refreshed, messages);
    }

    private TicketMessage buildMessage(User sender, TicketSenderType senderType, String content, List<MultipartFile> images) throws IOException {
        TicketMessage message = new TicketMessage();
        message.setSender(sender);
        message.setSenderType(senderType);
        message.setContent(content.trim());

        for (String imageUrl : uploadImages(images)) {
            TicketMessageImage image = new TicketMessageImage();
            image.setImageUrl(imageUrl);
            message.addImage(image);
        }
        return message;
    }

    private List<String> uploadImages(List<MultipartFile> images) throws IOException {
        List<String> uploaded = new ArrayList<>();
        if (images == null) {
            return uploaded;
        }
        for (MultipartFile image : images) {
            if (image == null || image.isEmpty()) {
                continue;
            }
            String fileName = fileService.uploadImage(imagePath, image);
            uploaded.add(buildImageUrl(fileName));
        }
        return uploaded;
    }

    private String buildImageUrl(String fileName) {
        return imageBaseUrl.endsWith("/") ? imageBaseUrl + fileName : imageBaseUrl + "/" + fileName;
    }

    private Ticket loadTicket(Long ticketId) {
        return ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", "ticketId", ticketId));
    }

    private Ticket loadTicketForCurrentCustomer(Long ticketId) {
        Ticket ticket = loadTicket(ticketId);
        Long currentUserId = authUtil.getCurrentUserId();
        if (ticket.getCustomer() == null || !ticket.getCustomer().getUserId().equals(currentUserId)) {
            throw new ForbiddenException("You do not have permission to access this ticket.");
        }
        return ticket;
    }

    private User currentUser() {
        Long currentUserId = authUtil.getCurrentUserId();
        return userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "userId", currentUserId));
    }

    private Sort buildSort(String sortBy, String sortOrder) {
        return "asc".equalsIgnoreCase(sortOrder)
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
    }
}