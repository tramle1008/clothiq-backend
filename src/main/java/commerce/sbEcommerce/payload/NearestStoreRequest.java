package commerce.sbEcommerce.payload;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NearestStoreRequest {
    @NotBlank(message = "Address is required")
    private String address;
}
