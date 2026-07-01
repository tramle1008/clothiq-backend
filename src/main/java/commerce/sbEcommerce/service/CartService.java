package commerce.sbEcommerce.service;

import commerce.sbEcommerce.model.Cart;
import commerce.sbEcommerce.payload.CartDTO;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


public interface CartService {
    CartDTO addProductToCart(Long ProductId, Integer quantity);

    Cart getOrCreateUserCart();

    List<CartDTO> getAllCarts();

    CartDTO getCart(String userEmail, Long cartId);

    @Transactional
    CartDTO updateProductQuantityInCart(Long productId, Integer quantity);

    @Transactional
    CartDTO setProductQuantityInCart(Long productId, Integer quantity);

    String deleteProductFromCart(Long productId);
}
