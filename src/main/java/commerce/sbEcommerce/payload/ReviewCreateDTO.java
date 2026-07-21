package commerce.sbEcommerce.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewCreateDTO {
    private Integer rating;
    private String comment;
    private Long productId;
}