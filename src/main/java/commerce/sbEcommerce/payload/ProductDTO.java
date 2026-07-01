package commerce.sbEcommerce.payload;

import commerce.sbEcommerce.model.RecordStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {
    private Long productId;
    private String productCode;
    private String productName;
    private String image;
    private String description;
    private Integer quantity;
    private Double salePrice;
    private Double costPrice;
    private Double discount;
    private RecordStatus status;
    private Long categoryId;

}
