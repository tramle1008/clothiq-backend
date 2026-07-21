package commerce.sbEcommerce.repository;

import commerce.sbEcommerce.model.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartRepository extends JpaRepository<Cart, Long> {
    Cart findByUser_EmailAndCartId(String email, Long cartId);
    Cart findByUser_Email(String emailId);
}
