package commerce.sbEcommerce.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WardDTO {
    private Long wardId;
    private String name;
    private Long provinceId;
}
