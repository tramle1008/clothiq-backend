package commerce.sbEcommerce.service;

import commerce.sbEcommerce.exceptioons.APIException;
import commerce.sbEcommerce.exceptioons.ResourceNotFoundException;
import commerce.sbEcommerce.model.Category;
import commerce.sbEcommerce.model.Product;
import commerce.sbEcommerce.model.User;
import commerce.sbEcommerce.payload.ProductDTO;
import commerce.sbEcommerce.payload.ProductResponse;
import commerce.sbEcommerce.repository.CategoryRepository;
import commerce.sbEcommerce.repository.ProductRepository;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public  class ProductServiceIml implements  ProductService {

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
//ban 1
    public ProductDTO addProduct(Long categoryId, ProductDTO dto, MultipartFile imageFile) throws IOException {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục"));

        String filename = null;
        if (imageFile != null && !imageFile.isEmpty()) {
            filename = UUID.randomUUID() + "_" + imageFile.getOriginalFilename();
            Path imagePath = Paths.get(path, filename);
            Files.copy(imageFile.getInputStream(), imagePath, StandardCopyOption.REPLACE_EXISTING);
        }

        Product product = new Product();
        product.setProductName(dto.getProductName());
        product.setDescription(dto.getDescription());
        product.setQuantity(dto.getQuantity());
        product.setPrice(dto.getPrice());
        product.setDiscount(dto.getDiscount());
        product.setSpecialPrice(dto.getSpecialPrice());
        product.setImage(filename);
        product.setCategory(category);


        Product saved = productRepository.save(product);

        // Mapping lại DTO để trả về
        ProductDTO response = new ProductDTO();
        response.setProductId(saved.getProductId());
        response.setProductName(saved.getProductName());
        response.setImage(saved.getImage());
        response.setDescription(saved.getDescription());
        response.setPrice(saved.getPrice());
        response.setDiscount(saved.getDiscount());
        response.setSpecialPrice(saved.getSpecialPrice());
        response.setQuantity(saved.getQuantity());

        return response;
    }


//lan 2
@Override
public ProductDTO addProduct_Image(Long categoryId, ProductDTO dto, MultipartFile imageFile) throws IOException {
    Category category = categoryRepository.findById(categoryId)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục"));

    String filename = DEFAULT_PRODUCT_IMAGE;
    if (imageFile != null && !imageFile.isEmpty()) {
        filename = fileService.uploadImage(path, imageFile);
    }

    Product product = modelMapper.map(dto, Product.class);
    product.setImage(filename);
    product.setCategory(category);

    Product saved = productRepository.save(product);

    ProductDTO response = modelMapper.map(saved, ProductDTO.class);
    response.setImage(imageBaseURL + "/" + saved.getImage());

    return response;
}

    @Override
    public ProductDTO getProductById(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đươc sản phẩm"));
        ProductDTO response = modelMapper.map(product, ProductDTO.class);
        return response;
    }


    @Override
    public ProductDTO addProductDefault(Long categoryId, ProductDTO productDTO) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục"));


        String filename = "defaultImage.jpg";
        Product product = new Product();
        product.setProductName(productDTO.getProductName());
        product.setProductCode(productDTO.getProductCode());
        product.setDescription(productDTO.getDescription());
        product.setQuantity(productDTO.getQuantity());
        product.setPrice(productDTO.getPrice());
        product.setDiscount(productDTO.getDiscount());
        double specialPrice = productDTO.getPrice() - 0.01*productDTO.getPrice()*productDTO.getDiscount();
        product.setSpecialPrice(specialPrice);
        product.setImage(filename);
        product.setCategory(category);


        Product saved = productRepository.save(product);

        // Mapping lại DTO để trả về
        ProductDTO response = new ProductDTO();
        response.setProductId(saved.getProductId());
        response.setProductName(saved.getProductName());
        response.setProductCode(saved.getProductCode());
        response.setImage(saved.getImage());
        response.setDescription(saved.getDescription());
        response.setPrice(saved.getPrice());
        response.setDiscount(saved.getDiscount());
        response.setSpecialPrice(saved.getSpecialPrice());
        response.setQuantity(saved.getQuantity());

        return response;
    }

    @Override
    public ProductResponse getAllProduct(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder, String key, Integer categoryId) {
        Sort sortByAnOrder = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by((sortBy)).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageDetail = PageRequest.of(pageNumber, pageSize, sortByAnOrder);
        Specification<Product> spec = Specification.where(null);
        if (key != null && !key.isEmpty()) {
            spec = spec.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.like(
                            criteriaBuilder.lower(root.get("productName")),
                            "%" + key.toLowerCase() + "%"
                    )
            );
        }

        if (categoryId != null) {
            spec = spec.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("category").get("categoryId"), categoryId)
            );
        }
        Page<Product> pageProduct = productRepository.findAll(spec,pageDetail);

        List<Product> products = pageProduct.getContent();
        List<ProductDTO> productDTOS = products.stream()
              .map(product -> {
                 ProductDTO productDTO = modelMapper.map(product, ProductDTO.class);
                 productDTO.setImage(contructImageUrl(product.getImage()));
                 return productDTO;
              })
              .collect(Collectors.toList());
//        if(products.isEmpty()){
//          throw  new APIException("No product exits");
//         }
          ProductResponse productResponse = new ProductResponse();
          productResponse.setContent(productDTOS);
          productResponse.setPageNumber(pageProduct.getNumber());
          productResponse.setPageSize(pageProduct.getSize());
          productResponse.setTotalElements(pageProduct.getTotalElements());
          productResponse.setTotalPages(pageProduct.getTotalPages());
          productResponse.setLastPage(pageProduct.isLast());
          return productResponse;
    }

    private String contructImageUrl(String imageName){
        return imageBaseURL.endsWith("/") ? imageBaseURL + imageName : imageBaseURL + "/" + imageName;
    }
    @Override
    public ProductResponse getProductByCategory(Long categoryId, Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(pageNumber, pageSize, sortByAndOrder);

        // Tìm danh mục
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "categoryId", categoryId));

        // Lấy sản phẩm phân trang theo danh mục
        Page<Product> pageProduct =  productRepository.findByCategory(category, pageable);

        // Chuyển sang DTO
        List<ProductDTO> productDTOS = pageProduct.getContent().stream()
                .map(product -> modelMapper.map(product, ProductDTO.class))
                .collect(Collectors.toList());

        // Gói ProductResponse
        ProductResponse productResponse = new ProductResponse();
        productResponse.setContent(productDTOS);
        productResponse.setPageNumber(pageProduct.getNumber());
        productResponse.setPageSize(pageProduct.getSize());
        productResponse.setTotalElements(pageProduct.getTotalElements());
        productResponse.setTotalPages(pageProduct.getTotalPages());
        productResponse.setLastPage(pageProduct.isLast());

        return productResponse;
    }


@Override
public ProductResponse getProductByKey(String key, Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
    Sort sort = sortOrder.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending()
            : Sort.by(sortBy).descending();
    Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);

    Page<Product> productPage = productRepository
            .findByProductNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(key, key, pageable);

    List<ProductDTO> productDTOs = productPage.getContent().stream()
            .map(product -> modelMapper.map(product, ProductDTO.class))
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

    @Override
    public ProductResponse searchProductsByCodeOrName(String key, Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        Sort sort = sortOrder.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);

        Page<Product> productPage = productRepository
                .findByProductCodeContainingIgnoreCaseOrProductNameContainingIgnoreCase(key, key, pageable);

        List<ProductDTO> productDTOs = productPage.getContent().stream()
                .map(product -> {
                    ProductDTO productDTO = modelMapper.map(product, ProductDTO.class);
                    productDTO.setImage(contructImageUrl(product.getImage()));
                    return productDTO;
                })
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


    @Override
    public ProductDTO updateProduct(ProductDTO productDTO, Long productId) {
        // 1. Lấy product từ DB
        Product product1 = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));

        // 2. Cập nhật nếu có dữ liệu mới
        if (productDTO.getProductName() != null) {
            product1.setProductName(productDTO.getProductName());
        }

        if (productDTO.getProductCode() != null) {
            product1.setProductCode(productDTO.getProductCode());
        }

        if (productDTO.getDescription() != null) {
            product1.setDescription(productDTO.getDescription());
        }

        if (productDTO.getQuantity() != null) {
            product1.setQuantity(productDTO.getQuantity());
        }

        if (productDTO.getDiscount() != null) {
            product1.setDiscount(productDTO.getDiscount());
        }

        if (productDTO.getPrice() != null) {
            product1.setPrice(productDTO.getPrice());
        }

        // Chỉ tính specialPrice nếu cả price và discount có
        if (productDTO.getPrice() != null || productDTO.getDiscount() != null) {
            double specialPrice = productDTO.getPrice() - productDTO.getDiscount() * 0.01 * productDTO.getPrice();
            product1.setSpecialPrice(specialPrice);
        }

        // 3. Lưu lại
        Product saveProduct = productRepository.save(product1);

        // 4. Trả về kết quả
        return modelMapper.map(saveProduct, ProductDTO.class);
    }

    @Override
    public ProductDTO updateProductImage(Long productId, MultipartFile image) throws IOException {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("product", "productid", productId));

        String fileName = fileService.uploadImage(path, image);

        product.setImage(fileName);
        Product updateProduct = productRepository.save(product);
        return modelMapper.map(updateProduct,ProductDTO.class);

    }



    @Override
    public ProductDTO deleteProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("product","productId",productId));
        productRepository.delete(product) ;

        return modelMapper.map(product, ProductDTO.class);
    }



}
