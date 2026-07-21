package commerce.sbEcommerce.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private  Long productId;

    @NotBlank(message = "Product code không được trống")
    @Column(nullable = false, unique = true)
    private String productCode;

    @NotBlank(message = "Product name không được trống")
    private String productName;

    private String description;
    private Integer quantity;
    private String image;
//    giá bán ra chưa xét giảm giá
    private Double salePrice;
//    giá nhập kho
    private Double costPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RecordStatus status = RecordStatus.ACTIVE;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    @OneToMany(mappedBy = "product", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch =  FetchType.EAGER)
    List<CartItem> products = new ArrayList<>();

    @OneToMany(mappedBy = "product",cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch =  FetchType.EAGER )
    private List<OrderItem> orderItems = new ArrayList<>();

    @ManyToMany(mappedBy = "products", fetch = FetchType.EAGER)
    @ToString.Exclude
    private List<Discount> discounts = new ArrayList<>();

    // New fields for product reviews
    private Double averageRating = 0.0;
    private Integer numberOfReviews = 0;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private List<ProductReview> productReviews = new ArrayList<>();
}
