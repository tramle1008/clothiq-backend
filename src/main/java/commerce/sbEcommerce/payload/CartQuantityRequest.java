package commerce.sbEcommerce.payload;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CartQuantityRequest {
    @NotNull(message = "Quantity khong duoc null")
    @Min(value = 1, message = "Quantity phai lon hon 0")
    private Integer quantity;
}
