package commerce.sbEcommerce.service;

import commerce.sbEcommerce.exceptioons.APIException;
import commerce.sbEcommerce.exceptioons.ResourceNotFoundException;
import commerce.sbEcommerce.model.Cart;
import commerce.sbEcommerce.model.CartItem;
import commerce.sbEcommerce.model.Discount;
import commerce.sbEcommerce.model.Product;
import commerce.sbEcommerce.model.User;
import commerce.sbEcommerce.payload.CartDTO;
import commerce.sbEcommerce.payload.ProductDTO;
import commerce.sbEcommerce.repository.CartItemRepository;
import commerce.sbEcommerce.repository.CartRepository;
import commerce.sbEcommerce.repository.ProductRepository;
import commerce.sbEcommerce.util.AuthUtil;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Transactional
@Service
public class CartServiceIml implements CartService {

    @Autowired
    CartRepository cartRepository;

    @Autowired
    AuthUtil authUtil;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    CartItemRepository cartItemRepository;

    @Autowired
    ModelMapper modelMapper;

    @Override
    public CartDTO addProductToCart(Long productId, Integer quantity) {
        validateRequestedQuantity(quantity);
        Cart cart = getOrCreateUserCart();
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        CartItem cartItem = cartItemRepository
                .findByProduct_ProductIdAndCart_CartId(productId, cart.getCartId())
                .orElse(null);

        if (cartItem != null) {
            throw new RuntimeException("Product is already exist! ");
        }
        if (product.getQuantity() == 0) {
            throw new RuntimeException("Product is not available! ");
        }
        if (product.getQuantity() < quantity) {
            throw new RuntimeException("Please, make an order less than or equal the quantity available ! ");
        }

        CartItem newCartItem = new CartItem();
        newCartItem.setProduct(product);
        newCartItem.setCart(cart);
        newCartItem.setQuantity(quantity);
        cartItemRepository.save(newCartItem);
        cart.getCartItems().add(newCartItem);

        return toCartDto(cart);
    }

    @Override
    public Cart getOrCreateUserCart() {
        User user = authUtil.getCurrentUserEntity();
        Cart userCart = cartRepository.findByUser_Email(user.getEmail());

        if (userCart == null) {
            userCart = new Cart();
            userCart.setUser(user);
            userCart = cartRepository.save(userCart);
        }

        return userCart;
    }

    @Override
    public List<CartDTO> getAllCarts() {
        List<Cart> carts = cartRepository.findAll();
        if (carts.isEmpty()) {
            throw new APIException("No cart exist");
        }

        return carts.stream()
                .map(this::toCartDto)
                .collect(Collectors.toList());
    }

    @Override
    public CartDTO getCart(String userEmail, Long cartId) {
        Cart cart = cartRepository.findByUser_EmailAndCartId(userEmail, cartId);
        if (cart == null) {
            throw new RuntimeException("Not find user");
        }

        return toCartDto(cart);
    }

    @Override
    public CartDTO updateProductQuantityInCart(Long productId, Integer quantity) {
        String userEmail = authUtil.getCurrentUserEmail();
        Cart cart = cartRepository.findByUser_Email(userEmail);
        if (cart == null) {
            throw new RuntimeException("Cart not found");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        CartItem cartItem = cartItemRepository
                .findByProduct_ProductIdAndCart_CartId(productId, cart.getCartId())
                .orElseThrow(() -> new APIException("Product " + product.getProductName() + " not available!"));

        int newQuantity = cartItem.getQuantity() + quantity;
        if (newQuantity < 0) {
            throw new RuntimeException("Quantity cannot be negative!");
        }

        if (newQuantity > product.getQuantity()) {
            throw new RuntimeException("Please, make an order less than or equal the quantity available!");
        }

        if (newQuantity == 0) {
            cartItemRepository.delete(cartItem);
            cart.getCartItems().removeIf(item -> item.getCartItemId().equals(cartItem.getCartItemId()));
        } else {
            cartItem.setQuantity(newQuantity);
            cartItemRepository.save(cartItem);
        }

        return toCartDto(cart);
    }

    @Override
    public CartDTO setProductQuantityInCart(Long productId, Integer quantity) {
        validateRequestedQuantity(quantity);

        String userEmail = authUtil.getCurrentUserEmail();
        Cart cart = cartRepository.findByUser_Email(userEmail);
        if (cart == null) {
            throw new RuntimeException("Cart not found");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        CartItem cartItem = cartItemRepository
                .findByProduct_ProductIdAndCart_CartId(productId, cart.getCartId())
                .orElseThrow(() -> new APIException("Product " + product.getProductName() + " not available!"));

        if (quantity > product.getQuantity()) {
            throw new APIException("Please, make an order less than or equal the quantity available!");
        }

        cartItem.setQuantity(quantity);
        cartItemRepository.save(cartItem);

        return toCartDto(cart);
    }

    @Override
    public String deleteProductFromCart(Long productId) {
        String userEmail = authUtil.getCurrentUserEmail();
        Cart userCart = cartRepository.findByUser_Email(userEmail);
        Long cartId = userCart.getCartId();

        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "CartId", cartId));

        CartItem cartItem = cartItemRepository.findByProduct_ProductIdAndCart_CartId(productId, cartId).orElse(null);
        if (cartItem == null) {
            throw new ResourceNotFoundException("Product", "ProductId", productId);
        }

        String productName = cartItem.getProduct().getProductName();
        cart.getCartItems().removeIf(item -> item.getCartItemId().equals(cartItem.getCartItemId()));
        cartItemRepository.deleteCartItemByProductIdAndCartId(cartId, productId);
        return "Sáº£n pháº©m " + productName + " Ä‘Ã£ bá»‹ xÃ³a khá»i giá» hÃ ng";
    }

    private double getCurrentDiscountPercent(Product product) {
        LocalDateTime now = LocalDateTime.now();
        return product.getDiscounts().stream()
                .filter(discount -> Boolean.TRUE.equals(discount.getActive()))
                .filter(discount -> discount.getStartDate() == null || !discount.getStartDate().isAfter(now))
                .filter(discount -> discount.getEndDate() == null || !discount.getEndDate().isBefore(now))
                .map(Discount::getPercent)
                .findFirst()
                .orElse(0.0);
    }

    private double calculateDiscountedPrice(Product product) {
        double salePrice = product.getSalePrice() != null ? product.getSalePrice() : 0.0;
        double discountPercent = getCurrentDiscountPercent(product);
        return salePrice - (salePrice * discountPercent / 100);
    }

    private void validateRequestedQuantity(Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new APIException("Quantity must be greater than 0");
        }
    }

    private CartDTO toCartDto(Cart cart) {
        CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);
        List<ProductDTO> productDTOs = cart.getCartItems().stream()
                .map(item -> {
                    ProductDTO dto = modelMapper.map(item.getProduct(), ProductDTO.class);
                    dto.setQuantity(item.getQuantity());
                    dto.setDiscount(getCurrentDiscountPercent(item.getProduct()));
                    return dto;
                })
                .toList();
        cartDTO.setProducts(productDTOs);
        cartDTO.setTotalPrice(
                cart.getCartItems().stream()
                        .mapToDouble(item -> calculateDiscountedPrice(item.getProduct()) * item.getQuantity())
                        .sum()
        );
        return cartDTO;
    }
}
