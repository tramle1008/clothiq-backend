package commerce.sbEcommerce.repository;

import commerce.sbEcommerce.model.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    boolean existsByProduct_ProductIdAndOrder_Address_User_UserId(Long productId, Long userId);
}
