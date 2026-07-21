package commerce.sbEcommerce.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductReviewDTO {
    private Long id;
    private Integer rating;
    private String comment;
    private LocalDateTime reviewDate;
    private Long userId;
    private String userName;
    private Long productId;
    private String productName;
}
