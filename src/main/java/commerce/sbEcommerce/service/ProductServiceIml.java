package commerce.sbEcommerce.service;

import commerce.sbEcommerce.exceptioons.APIException;
import commerce.sbEcommerce.exceptioons.ResourceNotFoundException;
import commerce.sbEcommerce.model.Category;
import commerce.sbEcommerce.model.Discount;
import commerce.sbEcommerce.model.Product;
import commerce.sbEcommerce.model.RecordStatus;
import commerce.sbEcommerce.payload.ProductDTO;
import commerce.sbEcommerce.payload.ProductResponse;
import commerce.sbEcommerce.repository.CategoryRepository;
import commerce.sbEcommerce.repository.ProductRepository;
import org.modelmapper.AbstractCondition;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductServiceIml implements ProductService {

    @Autowired
    ProductRepository productRepository;

    @Autowired
    CategoryRepository categoryRepository;

    @Autowired
    FileService fileService;

    @Autowired
    private ModelMapper modelMapper;

    private static final String DEFAULT_PRODUCT_IMAGE = "default.jpg";

    @Value("${project.image}")
    private String path;

    @Value("${image.base.url}")
    private String imageBaseURL;

    @Override
    public ProductDTO addProduct(Long categoryId, ProductDTO dto, MultipartFile imageFile) throws IOException {
        Category category = findCategoryForWrite(categoryId);

        String filename = DEFAULT_PRODUCT_IMAGE;
        if (imageFile != null && !imageFile.isEmpty()) {
            filename = fileService.uploadImage(path, imageFile);
        }

        Product product = new Product();
        product.setProductCode(dto.getProductCode());
        product.setProductName(dto.getProductName());
        product.setDescription(dto.getDescription());
        product.setQuantity(dto.getQuantity());
        product.setSalePrice(dto.getSalePrice() != null ? dto.getSalePrice() : 0D);
        product.setCostPrice(dto.getCostPrice() != null ? dto.getCostPrice() : 0D);
        product.setImage(filename);
        product.setStatus(dto.getStatus() != null ? dto.getStatus() : RecordStatus.ACTIVE);
        product.setCategory(category);
        Product saved = productRepository.save(product);
        return mapProduct(saved);
    }

    @Override
    public ProductDTO addProduct_Image(Long categoryId, ProductDTO dto, MultipartFile imageFile) throws IOException {
        Category category = findCategoryForWrite(categoryId);

        String filename = DEFAULT_PRODUCT_IMAGE;
        if (imageFile != null && !imageFile.isEmpty()) {
            filename = fileService.uploadImage(path, imageFile);
        }

        Product product = modelMapper.map(dto, Product.class);
        product.setImage(filename);
        product.setCategory(category);
        if (product.getStatus() == null) {
            product.setStatus(RecordStatus.ACTIVE);
        }
        Product saved = productRepository.save(product);
        return mapProduct(saved);
    }

    @Override
    public ProductDTO getProductById(Long productId) {
        Product product = productRepository.findOne(buildProductSpecification(null, null, null, null, List.of(RecordStatus.ACTIVE), true)
                        .and((root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("productId"), productId)))
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));
        return mapProduct(product);
    }



    @Override
    public ProductDTO addProductDefault(Long categoryId, ProductDTO productDTO) {
        Category category = findCategoryForWrite(categoryId);

        Product product = new Product();
        product.setProductName(productDTO.getProductName());
        product.setProductCode(productDTO.getProductCode());
        product.setDescription(productDTO.getDescription());
        product.setQuantity(productDTO.getQuantity());
        product.setSalePrice(productDTO.getSalePrice() != null ? productDTO.getSalePrice() : 0D);
        product.setCostPrice(productDTO.getCostPrice() != null ? productDTO.getCostPrice() : 0D);
        product.setImage(DEFAULT_PRODUCT_IMAGE);
        product.setStatus(productDTO.getStatus() != null ? productDTO.getStatus() : RecordStatus.ACTIVE);
        product.setCategory(category);
        Product saved = productRepository.save(product);
        return mapProduct(saved);
    }

    @Override
    public ProductResponse getAllProduct(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder, String key, Integer categoryId, Double minPrice, Double maxPrice) {
        return getProducts(pageNumber, pageSize, sortBy, sortOrder, key, categoryId, minPrice, maxPrice, List.of(RecordStatus.ACTIVE), true);
    }

    @Override
    public ProductResponse getAllProductForAdmin(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder, String key, Integer categoryId, Double minPrice, Double maxPrice, List<RecordStatus> statuses) {
        return getProducts(pageNumber, pageSize, sortBy, sortOrder, key, categoryId, minPrice, maxPrice, resolveStatuses(statuses, true), false);
    }

    @Override
    public ProductResponse getProductByCategory(Long categoryId, Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        Category category = categoryRepository.findOne(categoryHasIdAndStatus(categoryId, RecordStatus.ACTIVE))
                .orElseThrow(() -> new ResourceNotFoundException("Category", "categoryId", categoryId));

        return getProducts(pageNumber, pageSize, sortBy, sortOrder, null, Math.toIntExact(category.getCategoryId()), null, null, List.of(RecordStatus.ACTIVE), true);
    }

    @Override
    public ProductResponse getProductByCategoryForAdmin(Long categoryId, Integer pageNumber, Integer pageSize, String sortBy, String sortOrder, List<RecordStatus> statuses) {
        categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "categoryId", categoryId));
        return getProducts(pageNumber, pageSize, sortBy, sortOrder, null, Math.toIntExact(categoryId), null, null, resolveStatuses(statuses, true), false);
    }

    @Override
    public ProductResponse getProductByKey(String key, Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        return getProducts(pageNumber, pageSize, sortBy, sortOrder, key, null, null, null, List.of(RecordStatus.ACTIVE), true);
    }

    @Override
    public ProductResponse searchProductsByCodeOrName(String key, Integer pageNumber, Integer pageSize, String sortBy, String sortOrder, Double minPrice, Double maxPrice) {
        return searchProducts(pageNumber, pageSize, sortBy, sortOrder, key, minPrice, maxPrice, List.of(RecordStatus.ACTIVE), true);
    }

    @Override
    public ProductResponse searchProductsByCodeOrNameForAdmin(String key, Integer pageNumber, Integer pageSize, String sortBy, String sortOrder, Double minPrice, Double maxPrice, List<RecordStatus> statuses) {
        return searchProducts(pageNumber, pageSize, sortBy, sortOrder, key, minPrice, maxPrice, resolveStatuses(statuses, true), false);
    }

    @Override
    public ProductDTO updateProduct(ProductDTO productDTO, Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));

        if (productDTO.getProductName() != null) {
            product.setProductName(productDTO.getProductName());
        }
        if (productDTO.getProductCode() != null) {
            product.setProductCode(productDTO.getProductCode());
        }
        if (productDTO.getDescription() != null) {
            product.setDescription(productDTO.getDescription());
        }
        if (productDTO.getQuantity() != null) {
            product.setQuantity(productDTO.getQuantity());
        }
        if (productDTO.getSalePrice() != null) {
            product.setSalePrice(productDTO.getSalePrice());
        }
        if (productDTO.getCostPrice() != null) {
            product.setCostPrice(productDTO.getCostPrice());
        }
        if (productDTO.getStatus() != null) {
            product.setStatus(productDTO.getStatus());
        }
        if(productDTO.getCategoryId() != null){
            product.setCategory(findCategoryForWrite(productDTO.getCategoryId()));
        }


        Product saveProduct = productRepository.save(product);
        return mapProduct(saveProduct);
    }

    @Override
    public ProductDTO updateProductImage(Long productId, MultipartFile image) throws IOException {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));
        String fileName = fileService.uploadImage(path, image);
        product.setImage(fileName);
        Product updateProduct = productRepository.save(product);
        return mapProduct(updateProduct);
    }

    @Override
    public ProductDTO deleteProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));
        product.setStatus(RecordStatus.DELETED);
        Product savedProduct = productRepository.save(product);
        return mapProduct(savedProduct);
    }

    private ProductResponse getProducts(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder, String key, Integer categoryId, Double minPrice, Double maxPrice, List<RecordStatus> statuses, boolean requireActiveCategory) {
        Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageDetail = PageRequest.of(pageNumber, pageSize, sortByAndOrder);
        Specification<Product> spec = buildProductSpecification(key, categoryId, minPrice, maxPrice, statuses, requireActiveCategory);
        Page<Product> pageProduct = productRepository.findAll(spec, pageDetail);

        List<ProductDTO> productDTOS = pageProduct.getContent().stream()
                .map(this::mapProduct)
                .collect(Collectors.toList());

        ProductResponse productResponse = new ProductResponse();
        productResponse.setContent(productDTOS);
        productResponse.setPageNumber(pageProduct.getNumber());
        productResponse.setPageSize(pageProduct.getSize());
        productResponse.setTotalElements(pageProduct.getTotalElements());
        productResponse.setTotalPages(pageProduct.getTotalPages());
        productResponse.setLastPage(pageProduct.isLast());
        return productResponse;
    }

    private ProductResponse searchProducts(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder, String key, Double minPrice, Double maxPrice, List<RecordStatus> statuses, boolean requireActiveCategory) {
        Sort sort = sortOrder.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);

        Specification<Product> spec = buildProductSpecification(null, null, minPrice, maxPrice, statuses, requireActiveCategory);
        if (key != null && !key.isBlank()) {
            String normalizedKey = "%" + key.toLowerCase() + "%";
            spec = spec.and((root, query, criteriaBuilder) -> criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("productCode")), normalizedKey),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("productName")), normalizedKey)
            ));
        }

        Page<Product> productPage = productRepository.findAll(spec, pageable);
        List<ProductDTO> productDTOs = productPage.getContent().stream()
                .map(this::mapProduct)
                .collect(Collectors.toList());

        ProductResponse response = new ProductResponse();
        response.setContent(productDTOs);
        response.setPageNumber(productPage.getNumber());
        response.setPageSize(productPage.getSize());
        response.setTotalElements(productPage.getTotalElements());
        response.setTotalPages(productPage.getTotalPages());
        response.setLastPage(productPage.isLast());
        return response;
    }

    private Specification<Product> buildProductSpecification(String key, Integer categoryId, Double minPrice, Double maxPrice, List<RecordStatus> statuses, boolean requireActiveCategory) {
        Specification<Product> spec = (root, query, criteriaBuilder) -> root.get("status").in(statuses);

        if (key != null && !key.isBlank()) {
            spec = spec.and((root, query, criteriaBuilder) -> criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("productName")), "%" + key.toLowerCase() + "%"),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), "%" + key.toLowerCase() + "%")
            ));
        }

        if (categoryId != null) {
            spec = spec.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("category").get("categoryId"), categoryId));
        }

        if (minPrice != null) {
            spec = spec.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.greaterThanOrEqualTo(root.get("salePrice"), minPrice));
        }

        if (maxPrice != null) {
            spec = spec.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.lessThanOrEqualTo(root.get("salePrice"), maxPrice));
        }

        if (requireActiveCategory) {
            spec = spec.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("category").get("status"), RecordStatus.ACTIVE));
        }

        return spec;
    }

    private Specification<Category> categoryHasIdAndStatus(Long categoryId, RecordStatus status) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.and(
                criteriaBuilder.equal(root.get("categoryId"), categoryId),
                criteriaBuilder.equal(root.get("status"), status)
        );
    }

    private Category findCategoryForWrite(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "categoryId", categoryId));
        if (category.getStatus() == RecordStatus.DELETED) {
            throw new APIException("Category is deleted and cannot be used.");
        }
        return category;
    }

    private List<RecordStatus> resolveStatuses(List<RecordStatus> statuses, boolean includeDeletedDefault) {
        if (statuses == null || statuses.isEmpty()) {
            return includeDeletedDefault
                    ? List.of(RecordStatus.ACTIVE, RecordStatus.INACTIVE, RecordStatus.DELETED)
                    : List.of(RecordStatus.ACTIVE);
        }
        return statuses;
    }

    private ProductDTO mapProduct(Product product) {
        ProductDTO productDTO = modelMapper.map(product, ProductDTO.class);
        productDTO.setImage(contructImageUrl(product.getImage()));
        productDTO.setDiscount(getCurrentDiscountPercent(product));
        if (product.getCategory() != null) {
            productDTO.setCategoryId(product.getCategory().getCategoryId());
        }
        return productDTO;
    }

    private double getCurrentDiscountPercent(Product product) {
        LocalDateTime now = LocalDateTime.now();
        return product.getDiscounts().stream()
                .filter(discount -> Boolean.TRUE.equals(discount.getActive()))
                .filter(discount -> discount.getStartDate() == null || !discount.getStartDate().isAfter(now))
                .filter(discount -> discount.getEndDate() == null || !discount.getEndDate().isBefore(now))
                .map(Discount::getPercent)
                .findFirst()
                .orElse(0.0);
    }

    private String contructImageUrl(String imageName) {
        return imageBaseURL.endsWith("/") ? imageBaseURL + imageName : imageBaseURL + "/" + imageName;
    }
}
