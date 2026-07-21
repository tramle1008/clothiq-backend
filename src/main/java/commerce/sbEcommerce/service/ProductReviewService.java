package commerce.sbEcommerce.service;

import commerce.sbEcommerce.payload.ProductReviewDTO;
import commerce.sbEcommerce.payload.ReviewCreateDTO;

import java.util.List;

public interface ProductReviewService {
    ProductReviewDTO addReview(ReviewCreateDTO reviewCreateDTO);
    List<ProductReviewDTO> getReviewsByProductId(Long productId);
    Double getAverageRatingByProductId(Long productId);
    void deleteReview(Long reviewId);

    ProductReviewDTO updateReview(Long reviewId, ReviewCreateDTO reviewCreateDTO);
}
