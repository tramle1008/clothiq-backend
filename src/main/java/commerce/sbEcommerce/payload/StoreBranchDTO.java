package commerce.sbEcommerce.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StoreBranchDTO {
    private Long id;
    private String branchName;
    private String address;
    private String mapUrl;
    private String embedUrl;
    private Double latitude;
    private Double longitude;
}
