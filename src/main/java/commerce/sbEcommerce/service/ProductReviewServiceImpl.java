package commerce.sbEcommerce.service;

import commerce.sbEcommerce.exceptioons.ResourceNotFoundException;
import commerce.sbEcommerce.exceptioons.UnauthorizedException;
import commerce.sbEcommerce.model.Product;
import commerce.sbEcommerce.model.ProductReview;
import commerce.sbEcommerce.model.User;
import commerce.sbEcommerce.payload.ProductReviewDTO;
import commerce.sbEcommerce.payload.ReviewCreateDTO;
import commerce.sbEcommerce.repository.ProductReviewRepository;
import commerce.sbEcommerce.repository.ProductRepository;
import commerce.sbEcommerce.repository.UserRepository;
import commerce.sbEcommerce.repository.OrderItemRepository;
import commerce.sbEcommerce.util.AuthUtil; // Import AuthUtil
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductReviewServiceImpl implements ProductReviewService {

    @Autowired
    private ProductReviewRepository productReviewRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private AuthUtil authUtil; // Autowire AuthUtil

    @Override
    public ProductReviewDTO addReview(ReviewCreateDTO reviewCreateDTO) {
        Long currentUserId = authUtil.getCurrentUserId(); // Get current user ID
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUserId));

        Product product = productRepository.findById(reviewCreateDTO.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", reviewCreateDTO.getProductId()));

        // Validate if the user has purchased the product
        boolean hasPurchased = orderItemRepository.existsByProduct_ProductIdAndOrder_Address_User_UserId(
                reviewCreateDTO.getProductId(), currentUserId);

        if (!hasPurchased) {
            throw new UnauthorizedException("Bạn chưa mua sản phẩm này");
        }

        ProductReview productReview = new ProductReview();
        productReview.setRating(reviewCreateDTO.getRating());
        productReview.setComment(reviewCreateDTO.getComment());
        productReview.setReviewDate(LocalDateTime.now());
        productReview.setUser(user);
        productReview.setProduct(product);

        ProductReview savedReview = productReviewRepository.save(productReview);

        updateProductRating(product);

        return mapToDTO(savedReview);
    }

    @Override
    public List<ProductReviewDTO> getReviewsByProductId(Long productId) {
        List<ProductReview> reviews = productReviewRepository.findByProduct_ProductId(productId);
        return reviews.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Double getAverageRatingByProductId(Long productId) {
        List<ProductReview> reviews = productReviewRepository.findByProduct_ProductId(productId);
        if (reviews.isEmpty()) {
            return 0.0;
        }
        double sumRatings = reviews.stream().mapToInt(ProductReview::getRating).sum();
        return sumRatings / reviews.size();
    }

    @Override
    public void deleteReview(Long reviewId) {
        Long currentUserId = authUtil.getCurrentUserId(); // Get current user ID
        ProductReview review = productReviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("ProductReview", "id", reviewId));
        
        // Check if the current user is the owner of the review
        if (!review.getUser().getUserId().equals(currentUserId)) {
            throw new UnauthorizedException("Bạn không có quyền xóa đánh giá này");
        }

        productReviewRepository.delete(review);

        updateProductRating(review.getProduct());
    }

    @Override
    public ProductReviewDTO updateReview(Long reviewId, ReviewCreateDTO reviewCreateDTO) {
        Long currentUserId = authUtil.getCurrentUserId(); // Get current user ID
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUserId));

        Product product = productRepository.findById(reviewCreateDTO.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", reviewCreateDTO.getProductId()));

        // Validate if the current user is the owner of the review
        ProductReview existingReview = productReviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("ProductReview", "id", reviewId));

        if (!existingReview.getUser().getUserId().equals(currentUserId)) {
            throw new UnauthorizedException("Bạn không có quyền cập nhật đánh giá này");
        }

        // Validate if the user has purchased the product (optional, but good for consistency)
        boolean hasPurchased = orderItemRepository.existsByProduct_ProductIdAndOrder_Address_User_UserId(
                reviewCreateDTO.getProductId(), currentUserId);

        if (!hasPurchased) {
            throw new UnauthorizedException("Bạn chưa mua sản phẩm này");
        }

        existingReview.setRating(reviewCreateDTO.getRating());
        existingReview.setComment(reviewCreateDTO.getComment());
        existingReview.setReviewDate(LocalDateTime.now()); // Update review date on modification
        ProductReview savedReview = productReviewRepository.save(existingReview);

        updateProductRating(product);

        return mapToDTO(savedReview);
    }

    private ProductReviewDTO mapToDTO(ProductReview productReview) {
        ProductReviewDTO dto = modelMapper.map(productReview, ProductReviewDTO.class);
        dto.setUserId(productReview.getUser().getUserId());
        dto.setUserName(productReview.getUser().getUserName());
        dto.setProductId(productReview.getProduct().getProductId());
        dto.setProductName(productReview.getProduct().getProductName());
        return dto;
    }

    private void updateProductRating(Product product) {
        List<ProductReview> reviews = productReviewRepository.findByProduct_ProductId(product.getProductId());
        if (reviews.isEmpty()) {
            product.setAverageRating(0.0);
            product.setNumberOfReviews(0);
        } else {
            double sumRatings = reviews.stream().mapToInt(ProductReview::getRating).sum();
            product.setAverageRating(sumRatings / reviews.size());
            product.setNumberOfReviews(reviews.size());
        }
        productRepository.save(product);
    }
}