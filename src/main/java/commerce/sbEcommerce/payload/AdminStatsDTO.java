package commerce.sbEcommerce.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdminStatsDTO {
    private long totalUsers;
    private long totalProducts;
    private long totalOrders;
    private double revenueThisMonth;
    private long pendingDeliveries;
}
