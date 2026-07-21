package commerce.sbEcommerce.controller;

import commerce.sbEcommerce.payload.ProductReviewDTO;
import commerce.sbEcommerce.payload.ReviewCreateDTO;
import commerce.sbEcommerce.service.ProductReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ProductReviewController {

    @Autowired
    private ProductReviewService productReviewService;

    @PostMapping("/reviews")
    public ResponseEntity<ProductReviewDTO> addReview(@RequestBody ReviewCreateDTO reviewCreateDTO) {
        ProductReviewDTO newReview = productReviewService.addReview(reviewCreateDTO);
        return new ResponseEntity<>(newReview, HttpStatus.CREATED);
    }

    @GetMapping("/products/{productId}/reviews")
    public ResponseEntity<List<ProductReviewDTO>> getReviewsByProductId(@PathVariable Long productId) {
        List<ProductReviewDTO> reviews = productReviewService.getReviewsByProductId(productId);
        return new ResponseEntity<>(reviews, HttpStatus.OK);
    }

    @GetMapping("/products/{productId}/reviews/average")
    public ResponseEntity<Double> getAverageRatingByProductId(@PathVariable Long productId) {
        Double averageRating = productReviewService.getAverageRatingByProductId(productId);
        return new ResponseEntity<>(averageRating, HttpStatus.OK);
    }

    @DeleteMapping("/reviews/{reviewId}")
    public ResponseEntity<Void> deleteReview(@PathVariable Long reviewId) {
        productReviewService.deleteReview(reviewId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
    @PutMapping("/reviews/{reviewId}")
    public ResponseEntity<ProductReviewDTO> updateProductReview(
            @PathVariable Long reviewId,
            @RequestBody ReviewCreateDTO reviewCreateDTO
    ){
        ProductReviewDTO update = productReviewService.updateReview(reviewId, reviewCreateDTO);
        return new ResponseEntity<>(update, HttpStatus.OK);
    }
}