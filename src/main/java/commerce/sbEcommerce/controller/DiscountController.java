package commerce.sbEcommerce.controller;

import commerce.sbEcommerce.payload.DiscountCreateRequest;
import commerce.sbEcommerce.payload.DiscountProductRequest;
import commerce.sbEcommerce.payload.DiscountResponse;
import commerce.sbEcommerce.payload.DiscountStatusRequest;
import commerce.sbEcommerce.payload.DiscountUpdateRequest;
import commerce.sbEcommerce.service.DiscountService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/discounts")
public class DiscountController {

    private final DiscountService discountService;

    public DiscountController(DiscountService discountService) {
        this.discountService = discountService;
    }

    @GetMapping
    public ResponseEntity<List<DiscountResponse>> getAllDiscount() {
        return ResponseEntity.ok(discountService.getAllDiscount());
    }

    @PostMapping
    public ResponseEntity<DiscountResponse> createDiscount(@Valid @RequestBody DiscountCreateRequest request) {
        return new ResponseEntity<>(discountService.createDiscount(request), HttpStatus.CREATED);
    }

    @PostMapping("/{discountId}/products")
    public ResponseEntity<DiscountResponse> assignProducts(
            @PathVariable Long discountId,
            @Valid @RequestBody DiscountProductRequest request
    ) {
        return ResponseEntity.ok(discountService.assignProducts(discountId, request));
    }

    @DeleteMapping("/{discountId}/products")
    public ResponseEntity<DiscountResponse> unassignProducts(
            @PathVariable Long discountId,
            @Valid @RequestBody DiscountProductRequest request
    ) {
        return ResponseEntity.ok(discountService.unassignProducts(discountId, request));
    }

    @PutMapping("/{discountId}")
    public ResponseEntity<DiscountResponse> updateDiscount(
            @PathVariable Long discountId,
            @Valid @RequestBody DiscountUpdateRequest request
    ){
        return ResponseEntity.ok(discountService.updateDiscount(discountId, request));
    }

    @PutMapping("/{discountId}/status")
    public ResponseEntity<DiscountResponse> updateDiscountStatus(
            @PathVariable Long discountId,
            @Valid @RequestBody DiscountStatusRequest request
    ){
        return ResponseEntity.ok(discountService.updateDiscountStatus(discountId, request.getActive()));
    }

    @DeleteMapping("/{discountId}")
    public ResponseEntity<String> deleteDiscount(@PathVariable Long discountId) {
        discountService.deleteDiscount(discountId);
        return ResponseEntity.ok("Discount deleted successfully");
    }
}
