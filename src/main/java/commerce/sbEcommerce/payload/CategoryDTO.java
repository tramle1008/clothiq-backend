package commerce.sbEcommerce.payload;

import commerce.sbEcommerce.model.RecordStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDTO {
    private Long categoryId;
    private String categoryName;
    private RecordStatus status;
    private Long productCount;
}
