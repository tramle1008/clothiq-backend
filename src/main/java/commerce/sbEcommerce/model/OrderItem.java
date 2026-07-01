package commerce.sbEcommerce.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;


@Entity
@Data
@Table(name = "order_items")
@AllArgsConstructor
@NoArgsConstructor
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderItemId;

    private Integer quantity;

//    giảm giá tại thời điểm đặt hàng
    private double discount;
//    giá sản phẩm tại thời điểm đặt hàng
    private double orderProductPrice;

    @ManyToOne
    @JoinColumn(name = "product_id")
   private Product product;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;
}
