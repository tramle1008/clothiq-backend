package commerce.sbEcommerce.payload;

import commerce.sbEcommerce.model.DeliveryStatus;
import commerce.sbEcommerce.model.PaymentStatus;
import jakarta.validation.constraints.AssertTrue;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderSearchCriteria {
    private DeliveryStatus deliveryStatus;
    private PaymentStatus paymentStatus;
    private String paymentMethod;
    private String keyword;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fromDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate toDate;

    @AssertTrue(message = "toDate phai sau hoac bang fromDate")
    public boolean isDateRangeValid() {
        return fromDate == null || toDate == null || !toDate.isBefore(fromDate);
    }
}
