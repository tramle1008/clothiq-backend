package commerce.sbEcommerce.controller;

import commerce.sbEcommerce.model.PaymentStatus;
import commerce.sbEcommerce.payload.OrderDTO;
import commerce.sbEcommerce.payload.OrderSearchCriteria;
import commerce.sbEcommerce.repository.OrderRepository;
import commerce.sbEcommerce.service.OrderService;
import commerce.sbEcommerce.util.AuthUtil;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private AuthUtil authUtil;

    @Autowired
    private OrderRepository orderRepository;

    @GetMapping("/status/{orderCode}")
    public ResponseEntity<PaymentStatus> paymentStatus(@PathVariable String orderCode) {
        String email = authUtil.getCurrentUserEmail();
        PaymentStatus status = orderRepository.findPaymentStatusByCodeAndEmail(orderCode, email);
        return ResponseEntity.ok(status);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<Page<OrderDTO>> getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "dateOrder") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @Valid @ModelAttribute OrderSearchCriteria criteria) {
        Page<OrderDTO> orders = orderService.getAllOrdersPaginated(page, size, sortBy, sortDir, criteria);
        return new ResponseEntity<>(orders, HttpStatus.OK);
    }

    @GetMapping("/user")
    public ResponseEntity<Page<OrderDTO>> getUserOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "2") int size,
            @RequestParam(defaultValue = "dateOrder") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @Valid @ModelAttribute OrderSearchCriteria criteria) {

        Page<OrderDTO> orders = orderService.getUserOrderPaginated(page, size, sortBy, sortDir, criteria);
        return new ResponseEntity<>(orders, HttpStatus.OK);
    }
    @PutMapping("/{orderId}/reject")
    public ResponseEntity<OrderDTO> markRejected(@PathVariable Long orderId) {
        return new ResponseEntity<>(orderService.serviceMarkRejected(orderId), HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(value = "/shipping-slip", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> printShippingSlip(@RequestParam List<Long> orderIds) {
        byte[] pdf = orderService.generateShippingSlipPdf(orderIds);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=shipping-slip.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
    
}
