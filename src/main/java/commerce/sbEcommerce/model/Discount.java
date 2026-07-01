package commerce.sbEcommerce.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "discounts")
public class Discount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long discountId;

    @NotBlank(message = "Discount name khong duoc trong")
    @Column(nullable = false)
    private String name;

    @NotNull(message = "Percent khong duoc null")
    @DecimalMin(value = "0.0", message = "Percent phai lon hon hoac bang 0")
    @DecimalMax(value = "100.0", message = "Percent phai nho hon hoac bang 100")
    @Column(nullable = false)
    private Double percent;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    @Column(nullable = false)
    private Boolean active = Boolean.TRUE;

    @ManyToMany
    @JoinTable(
            name = "discount_products",
            joinColumns = @JoinColumn(name = "discount_id"),
            inverseJoinColumns = @JoinColumn(name = "product_id")
    )
    @ToString.Exclude
    private List<Product> products = new ArrayList<>();
}
