package commerce.sbEcommerce.payload;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
public class DiscountUpdateRequest {
    private static final ZoneOffset VIETNAM_OFFSET = ZoneOffset.ofHours(7);

    @Pattern(regexp = ".*\\S.*", message = "Discount name khong duoc trong")
    private String name;

    @DecimalMin(value = "0.0", message = "Percent phai lon hon hoac bang 0")
    @DecimalMax(value = "100.0", message = "Percent phai nho hon hoac bang 100")
    private Double percent;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
    private OffsetDateTime startDate;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
    private OffsetDateTime endDate;

    private Boolean active;

    @AssertTrue(message = "Start date va end date phai dung mui gio Viet Nam (+07:00)")
    public boolean isVietnamOffsetValid() {
        return hasVietnamOffset(startDate) && hasVietnamOffset(endDate);
    }

    @AssertTrue(message = "End date must be after or equal to start date")
    public boolean isDateRangeValid() {
        return startDate == null || endDate == null || !endDate.isBefore(startDate);
    }

    @AssertTrue(message = "Request update phai co it nhat 1 truong")
    public boolean hasAtLeastOneField() {
        return name != null || percent != null || startDate != null || endDate != null || active != null;
    }

    private boolean hasVietnamOffset(OffsetDateTime dateTime) {
        return dateTime == null || VIETNAM_OFFSET.equals(dateTime.getOffset());
    }
}
