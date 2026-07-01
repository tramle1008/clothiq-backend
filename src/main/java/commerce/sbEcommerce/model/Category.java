package commerce.sbEcommerce.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "categories")
@ToString
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long categoryId;

    @NotBlank
    @Size(min = 3, max = 100, message = "Category phải ít nhất 3-100 ký tự")
    private String categoryName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RecordStatus status = RecordStatus.ACTIVE;

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL)
    private List<Product> products;

}
