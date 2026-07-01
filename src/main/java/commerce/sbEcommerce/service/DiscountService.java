package commerce.sbEcommerce.service;

import commerce.sbEcommerce.payload.DiscountCreateRequest;
import commerce.sbEcommerce.payload.DiscountProductRequest;
import commerce.sbEcommerce.payload.DiscountResponse;
import commerce.sbEcommerce.payload.DiscountUpdateRequest;
import jakarta.validation.Valid;

import java.util.List;

public interface DiscountService {
    List<DiscountResponse> getAllDiscount();

    DiscountResponse createDiscount(DiscountCreateRequest request);

    DiscountResponse assignProducts(Long discountId, DiscountProductRequest request);

    DiscountResponse unassignProducts(Long discountId, DiscountProductRequest request);

    DiscountResponse updateDiscount(Long discountId, @Valid DiscountUpdateRequest request);

    DiscountResponse updateDiscountStatus(Long discountId, Boolean discountStatus);

    void deleteDiscount(Long discountId);
}
