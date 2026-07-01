package commerce.sbEcommerce.payload;

import commerce.sbEcommerce.model.DeliveryStatus;
import commerce.sbEcommerce.model.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDTO {
    private Long orderId;
    private String email;
    private List<OrderItemDTO> orderItemList;
    private PaymentStatus paymentStatus;
    private DeliveryStatus deliveryStatus;
    private String paymentMethod;
    private PaymentDTO payment;
    private LocalDate dateOrder;
    private Double totalAmount;
    private AddressDTO address;
    private String userName;

}
