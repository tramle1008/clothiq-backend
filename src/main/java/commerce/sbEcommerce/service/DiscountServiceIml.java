package commerce.sbEcommerce.service;

import commerce.sbEcommerce.exceptioons.APIException;
import commerce.sbEcommerce.exceptioons.ResourceNotFoundException;
import commerce.sbEcommerce.model.Discount;
import commerce.sbEcommerce.model.Product;
import commerce.sbEcommerce.payload.DiscountCreateRequest;
import commerce.sbEcommerce.payload.DiscountProductRequest;
import commerce.sbEcommerce.payload.DiscountResponse;
import commerce.sbEcommerce.payload.DiscountUpdateRequest;
import commerce.sbEcommerce.repository.DiscountRepository;
import commerce.sbEcommerce.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class DiscountServiceIml implements DiscountService {
    private static final ZoneOffset VIETNAM_OFFSET = ZoneOffset.ofHours(7);

    private final DiscountRepository discountRepository;
    private final ProductRepository productRepository;

    public DiscountServiceIml(DiscountRepository discountRepository, ProductRepository productRepository) {
        this.discountRepository = discountRepository;
        this.productRepository = productRepository;
    }

    @Override
    public List<DiscountResponse> getAllDiscount() {
        return discountRepository.findAll()
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public DiscountResponse createDiscount(DiscountCreateRequest request) {
        Discount discount = new Discount();
        discount.setName(request.getName());
        discount.setPercent(request.getPercent());
        discount.setStartDate(toVietnamLocalDateTime(request.getStartDate()));
        discount.setEndDate(toVietnamLocalDateTime(request.getEndDate()));
        discount.setActive(request.getActive() != null ? request.getActive() : Boolean.TRUE);

        return mapToDto(discountRepository.save(discount));
    }

    @Override
    @Transactional
    public DiscountResponse assignProducts(Long discountId, DiscountProductRequest request) {
        Discount discount = findDiscount(discountId);
        List<Product> products = findProducts(request.getProductIds());

        for (Product product : products) {
            if (!discount.getProducts().contains(product)) {
                discount.getProducts().add(product);
            }
            if (!product.getDiscounts().contains(discount)) {
                product.getDiscounts().add(discount);
            }
        }

        return mapToDto(discountRepository.save(discount));
    }

    @Override
    @Transactional
    public DiscountResponse unassignProducts(Long discountId, DiscountProductRequest request) {
        Discount discount = findDiscount(discountId);
        Set<Long> productIdsToRemove = new LinkedHashSet<>(request.getProductIds());

        discount.getProducts().removeIf(product -> {
            boolean shouldRemove = productIdsToRemove.contains(product.getProductId());
            if (shouldRemove) {
                product.getDiscounts().remove(discount);
            }
            return shouldRemove;
        });

        return mapToDto(discountRepository.save(discount));
    }

    @Override
    @Transactional
    public DiscountResponse updateDiscount(Long discountId, DiscountUpdateRequest request) {
      Discount discount = findDiscount(discountId);
      if (request.getStartDate() != null || request.getEndDate() != null) {
          validateDiscountDateRange(
                  request.getStartDate() != null ? toVietnamLocalDateTime(request.getStartDate()) : discount.getStartDate(),
                  request.getEndDate() != null ? toVietnamLocalDateTime(request.getEndDate()) : discount.getEndDate()
          );
      }
      if(request.getName() != null && !request.getName().isEmpty()) {
          discount.setName(request.getName());
      }
      if(request.getActive() != null) {
          discount.setActive(request.getActive());
      }
      if (request.getStartDate() != null) {
          discount.setStartDate(toVietnamLocalDateTime(request.getStartDate()));
      }
      if (request.getEndDate() != null) {
          discount.setEndDate(toVietnamLocalDateTime(request.getEndDate()));
      }
      if (request.getPercent() != null) {
          discount.setPercent(request.getPercent());
      }
      return mapToDto(discountRepository.save(discount));
    }

    @Override
    @Transactional
    public DiscountResponse updateDiscountStatus(Long discountId, Boolean discountStatus) {
      Discount discount = findDiscount(discountId);
      discount.setActive(discountStatus);
      return mapToDto(discountRepository.save(discount));
    }

    @Override
    @Transactional
    public void deleteDiscount(Long discountId) {
        Discount discount = findDiscount(discountId);

        for (Product product : new LinkedHashSet<>(discount.getProducts())) {
            product.getDiscounts().remove(discount);
        }
        discount.getProducts().clear();
        discountRepository.save(discount);
        discountRepository.delete(discount);
    }


    private Discount findDiscount(Long discountId) {
        return discountRepository.findById(discountId)
                .orElseThrow(() -> new ResourceNotFoundException("Discount", "discountId", discountId));
    }

    private List<Product> findProducts(List<Long> productIds) {
        Set<Long> uniqueProductIds = new LinkedHashSet<>(productIds);
        List<Product> products = productRepository.findAllById(uniqueProductIds);

        if (products.size() != uniqueProductIds.size()) {
            Set<Long> foundIds = products.stream()
                    .map(Product::getProductId)
                    .collect(Collectors.toSet());

            Long missingId = uniqueProductIds.stream()
                    .filter(id -> !foundIds.contains(id))
                    .findFirst()
                    .orElse(null);

            throw new ResourceNotFoundException("Product", "productId", missingId);
        }

        return products;
    }

    private void validateDiscountDateRange(java.time.LocalDateTime startDate, java.time.LocalDateTime endDate) {
        if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
            throw new APIException("End date must be after or equal to start date");
        }
    }

    private DiscountResponse mapToDto(Discount discount) {
        return new DiscountResponse(
                discount.getDiscountId(),
                discount.getName(),
                discount.getPercent(),
                toVietnamOffsetDateTime(discount.getStartDate()),
                toVietnamOffsetDateTime(discount.getEndDate()),
                discount.getActive(),
                discount.getProducts().stream()
                        .map(Product::getProductId)
                        .collect(Collectors.toList())
        );
    }

    private LocalDateTime toVietnamLocalDateTime(OffsetDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.atZoneSameInstant(VIETNAM_OFFSET).toLocalDateTime();
    }

    private OffsetDateTime toVietnamOffsetDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.atOffset(VIETNAM_OFFSET);
    }
}
