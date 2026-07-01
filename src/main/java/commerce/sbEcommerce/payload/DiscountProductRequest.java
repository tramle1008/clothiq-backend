package commerce.sbEcommerce.payload;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DiscountProductRequest {
    @NotEmpty(message = "productIds khong duoc rong")
    private List<@NotNull(message = "productId khong duoc null") Long> productIds;
}
