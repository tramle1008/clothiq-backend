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
    private double discount;
    private String image;
    private double price;
    private double specialPrice;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    @OneToMany(mappedBy = "product", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch =  FetchType.EAGER)
    List<CartItem> products = new ArrayList<>();

    @OneToMany(mappedBy = "product",cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch =  FetchType.EAGER )
    private List<OrderItem> orderItems = new ArrayList<>();

}
