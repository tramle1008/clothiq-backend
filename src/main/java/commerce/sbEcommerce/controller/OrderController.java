package commerce.sbEcommerce.controller;

import commerce.sbEcommerce.model.PaymentStatus;
import commerce.sbEcommerce.payload.OrderDTO;
import commerce.sbEcommerce.payload.OrderSearchCriteria;
import commerce.sbEcommerce.repository.OrderRepository;
import commerce.sbEcommerce.service.OrderService;
import commerce.sbEcommerce.util.AuthUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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
}
