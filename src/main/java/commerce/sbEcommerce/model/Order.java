package commerce.sbEcommerce.model;


import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name="orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderId;

    @Email
    @Column(nullable = false)
    private String email;


    @OneToMany(mappedBy = "order", cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE})
    List<OrderItem> orderItemList = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;

    @Enumerated(EnumType.STRING)
    private DeliveryStatus deliveryStatus;

    private LocalDate dateOrder;
    private Double totalAmount;

    @ManyToOne
    @JoinColumn(name = "address_id")
    private Address address;

    @OneToOne
    @JoinColumn(name = "payment_id")
    private Payment payment;

    @Column(name = "payment_method")
    private String paymentMethod;

    @Column(unique = true, nullable = false)
    private String code; // Mã đơn hàng, ví dụ: "DH123456"

}
