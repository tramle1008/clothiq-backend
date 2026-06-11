package commerce.sbEcommerce.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RevenueChartResponseDTO {
    private LocalDate fromDate;
    private LocalDate toDate;
    private String groupBy;
    private double totalRevenue;
    private List<RevenueChartPointDTO> points;
}
