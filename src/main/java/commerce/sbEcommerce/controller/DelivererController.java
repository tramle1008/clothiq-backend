package commerce.sbEcommerce.controller;

import commerce.sbEcommerce.config.AppConstants;
import commerce.sbEcommerce.payload.OrderDTO;
import commerce.sbEcommerce.payload.OrderSearchCriteria;
import commerce.sbEcommerce.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/api/deliver")
public class DelivererController {
    @Autowired
    OrderService orderService;

    @GetMapping("/orders")
    public ResponseEntity<Page<OrderDTO>> getOrderShipped(
            @RequestParam(name = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER) Integer pageNumber,
            @RequestParam(name = "pageSize", defaultValue = AppConstants.PAGE_SIZE_SHIPPED) Integer pageSize,
            @RequestParam(name = "sortBy", defaultValue = AppConstants.SORT_BY_ORDERED) String sortBy,
            @RequestParam(name = "sortOrder", defaultValue = AppConstants.SORT_ORDER_GIAM) String sortOrder,
            @Valid @ModelAttribute OrderSearchCriteria criteria
    ){
        return  new ResponseEntity<>(orderService.getOrderSHIPPED(pageNumber, pageSize, sortBy, sortOrder, criteria), HttpStatus.OK);
    }


    @PutMapping("/orders/{orderId}/delivered")
    public ResponseEntity<OrderDTO> delivererMarkDelivered(@PathVariable Long orderId) {
        return new ResponseEntity<>(orderService.delivererMarkDelivered(orderId), HttpStatus.OK);
    }

}
