package commerce.sbEcommerce.controller;

import commerce.sbEcommerce.model.Cart;
import commerce.sbEcommerce.payload.CartDTO;
import commerce.sbEcommerce.payload.CartQuantityRequest;
import commerce.sbEcommerce.repository.CartRepository;
import commerce.sbEcommerce.service.CartService;
import commerce.sbEcommerce.util.AuthUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api")
public class CartController {

    @Autowired
    CartService cartService;
    @Autowired
    AuthUtil authUtil;

    @Autowired
    CartRepository cartRepository;
    @PostMapping("/auth/cart/products/{productId}/quantity/{quantity}")
    public ResponseEntity<?> addProductToCart( @PathVariable Long productId,  @PathVariable Integer quantity){
        try {
            return new ResponseEntity<>(cartService.addProductToCart(productId, quantity), HttpStatus.CREATED);
        } catch (Exception ex) {
            ex.printStackTrace(); // để in ra log xem lỗi gì
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
        }
    }

    @GetMapping("/admin/carts")
    public ResponseEntity<List<CartDTO>> getCarts(){
        List<CartDTO> cartDTOS = cartService.getAllCarts();
        return new ResponseEntity<List<CartDTO>>(cartDTOS,HttpStatus.FOUND);
    }

    @GetMapping("/auth/carts/user/cart")
    public ResponseEntity<CartDTO> getCartById(){
        String userEmail= authUtil.getCurrentUserEmail();

        Cart userCart = cartRepository.findByUser_Email(userEmail);
        if (userCart == null) {
            // Trả về 200 với cart rỗng
            CartDTO emptyCart = new CartDTO();
            emptyCart.setTotalPrice(0.0);
            emptyCart.setProducts(new ArrayList<>()); // hoặc List.emptyList()
            return ResponseEntity.ok(emptyCart);
        }
        Long cartId = userCart.getCartId();
        return new ResponseEntity<CartDTO>(cartService.getCart(userEmail, cartId), HttpStatus.OK);
    }


    @PutMapping("/auth/user/cart/products/{productId}/quantity/{operation}")
    public ResponseEntity<CartDTO> updateCartProduct(@PathVariable  Long productId,@PathVariable String operation){
        CartDTO cartDTO = cartService.updateProductQuantityInCart(productId, operation.equalsIgnoreCase("delete") ? -1 : 1 );
        return  new ResponseEntity<CartDTO>(cartDTO, HttpStatus.OK);
    }

    @PutMapping("/auth/cart/{productId}/quantity")
    public ResponseEntity<CartDTO> setCartProductQuantity(
            @PathVariable Long productId,
            @Valid @RequestBody CartQuantityRequest request
    ) {
        CartDTO cartDTO = cartService.setProductQuantityInCart(productId, request.getQuantity());
        return new ResponseEntity<>(cartDTO, HttpStatus.OK);
    }


    @DeleteMapping("/auth/user/cart/product/{productId}")
    public ResponseEntity<String> deleteProductFromCart(
            @PathVariable Long productId){
        String status = cartService.deleteProductFromCart( productId);
        return new ResponseEntity<String>(status, HttpStatus.OK);
    }

}
