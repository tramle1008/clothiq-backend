package commerce.sbEcommerce.payload;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StoreBranchRequest {
    @NotBlank(message = "Branch name is required")
    private String branchName;

    @NotBlank(message = "Address is required")
    private String address;

    @NotBlank(message = "Map URL is required")
    private String mapUrl;

    @NotBlank(message = "Embed URL is required")
    private String embedUrl;
}
