package commerce.sbEcommerce.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RevenueChartPointDTO {
    private String period;
    private double revenue;
}
