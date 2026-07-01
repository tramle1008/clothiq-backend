package commerce.sbEcommerce.payload;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DiscountStatusRequest {
    @NotNull(message = "Discount status khong duoc null")
    private Boolean active;
}
