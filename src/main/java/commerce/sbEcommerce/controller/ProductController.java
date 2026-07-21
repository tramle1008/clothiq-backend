package commerce.sbEcommerce.controller;

import commerce.sbEcommerce.config.AppConstants;
import com.fasterxml.jackson.databind.ObjectMapper;
import commerce.sbEcommerce.model.RecordStatus;
import commerce.sbEcommerce.payload.ProductDTO;
import commerce.sbEcommerce.payload.ProductResponse;
import commerce.sbEcommerce.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api")
public class ProductController {
    @Autowired
    private ProductService productService;

    @Autowired
    private ObjectMapper objectMapper;

//    @PostMapping("/admin/categories/{categoryId}/product")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(
        value = "/admin/categories/{categoryId}/product",
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<ProductDTO> addProduct(
            @PathVariable Long categoryId,
            @RequestParam("product") String productJson,
            @RequestPart(value = "image", required = false) MultipartFile image
    ) throws IOException {
        ProductDTO productDTO = objectMapper.readValue(productJson, ProductDTO.class);
        ProductDTO addProduct = productService.addProduct(categoryId, productDTO, image);
        return  new ResponseEntity<>(addProduct, HttpStatus.CREATED);
    }


    @PostMapping("/admin/categories/{categoryId}/product/default")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductDTO> addProductDefault(
            @PathVariable Long categoryId,
            @Valid @RequestBody ProductDTO productDTO
    )  {
        ProductDTO addProduct = productService.addProductDefault(categoryId, productDTO);
        return  new ResponseEntity<>(addProduct, HttpStatus.CREATED);
    }

//thu lan 2
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/categories/{categoryId}/productImage")
    public ResponseEntity<ProductDTO> addProduct_Image(
            @PathVariable Long categoryId,
            @RequestPart("product") ProductDTO productDTO,
            @RequestPart(value = "imageFile", required = false) MultipartFile imageFile
    ) throws IOException {
        ProductDTO createdProduct = productService.addProduct_Image(categoryId, productDTO, imageFile);
        return ResponseEntity.ok(createdProduct);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/products")
    public ResponseEntity<ProductResponse> getAllProductForAdmin(
            @RequestParam(name =  "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
            @RequestParam(name =  "pageSize", defaultValue = AppConstants.PAGE_SIZE_PRODUCT, required = false ) Integer pageSize,
            @RequestParam(name =  "sortBy", defaultValue = AppConstants.SORT_BY_PRODUCTS, required = false ) String sortBy ,
            @RequestParam(name =  "sortOrder" , defaultValue = AppConstants.SORT_ORDER_TANG, required = false ) String sortOrder,
            @RequestParam(name = "key", required = false) String key,
            @RequestParam(name = "categoryId", required = false) Integer categoryId,
            @RequestParam(name = "minPrice", required = false) Double minPrice,
            @RequestParam(name = "maxPrice", required = false) Double maxPrice,
            @RequestParam(name = "statuses", required = false) List<RecordStatus> statuses){
        return new ResponseEntity<>(productService.getAllProductForAdmin(pageNumber, pageSize,sortBy,sortOrder ,key, categoryId, minPrice, maxPrice, statuses),HttpStatus.OK);
    }




    @GetMapping("/public/products")
    public ResponseEntity<ProductResponse> getAllProduct(
            @RequestParam(name =  "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
            @RequestParam(name =  "pageSize", defaultValue = AppConstants.PAGE_SIZE_PRODUCT, required = false ) Integer pageSize,
            @RequestParam(name =  "sortBy", defaultValue = AppConstants.SORT_BY_PRODUCTS, required = false ) String sortBy ,
            @RequestParam(name =  "sortOrder" , defaultValue = AppConstants.SORT_ORDER_TANG, required = false ) String sortOrder,
            @RequestParam(name = "key", required = false) String key,
            @RequestParam(name = "categoryId", required = false) Integer categoryId,
            @RequestParam(name = "minPrice", required = false) Double minPrice,
            @RequestParam(name = "maxPrice", required = false) Double maxPrice){
        return new ResponseEntity<>(productService.getAllProduct(pageNumber, pageSize,sortBy,sortOrder ,key, categoryId, minPrice, maxPrice ),HttpStatus.OK);
    }

    @GetMapping("/products/{productId}")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable Long productId){
        return new ResponseEntity<>(productService.getProductById(productId), HttpStatus.OK);
    }

    @GetMapping("/public/categories/{categoryId}/products")
    public ResponseEntity<ProductResponse> getProductByCategory(
            @PathVariable Long categoryId,
            @RequestParam(name =  "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
            @RequestParam(name =  "pageSize", defaultValue = AppConstants.PAGE_SIZE_PRODUCT, required = false ) Integer pageSize,
            @RequestParam(name =  "sortBy", defaultValue = AppConstants.SORT_BY_PRODUCTS, required = false ) String sortBy ,
            @RequestParam(name =  "sortOrder" , defaultValue = AppConstants.SORT_ORDER_TANG, required = false ) String sortOrder

    ){
        return new ResponseEntity<>(productService.getProductByCategory(categoryId, pageNumber, pageSize,sortBy,sortOrder), HttpStatus.OK);
    }

    @GetMapping("/public/categories/products")
    public ResponseEntity<ProductResponse> getProductByKey(
       @RequestParam(name = "key", defaultValue = "all") String key,
       @RequestParam(name =  "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
                                                           @RequestParam(name =  "pageSize", defaultValue = AppConstants.PAGE_SIZE_PRODUCT, required = false ) Integer pageSize,
                                                           @RequestParam(name =  "sortBy", defaultValue = AppConstants.SORT_BY_PRODUCTS, required = false ) String sortBy ,
                                                           @RequestParam(name =  "sortOrder" , defaultValue = AppConstants.SORT_ORDER_TANG, required = false ) String sortOrder){
        return new ResponseEntity<>(productService.getProductByKey(key, pageNumber, pageSize,sortBy,sortOrder), HttpStatus.OK);
    }

    @GetMapping("/public/products/search")
    public ResponseEntity<ProductResponse> searchProductsByCodeOrName(
            @RequestParam(name = "key") String key,
            @RequestParam(name = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
            @RequestParam(name = "pageSize", defaultValue = AppConstants.PAGE_SIZE_PRODUCT, required = false) Integer pageSize,
            @RequestParam(name = "sortBy", defaultValue = AppConstants.SORT_BY_PRODUCTS, required = false) String sortBy,
            @RequestParam(name = "sortOrder", defaultValue = AppConstants.SORT_ORDER_TANG, required = false) String sortOrder,
            @RequestParam(name = "minPrice", required = false) Double minPrice,
            @RequestParam(name = "maxPrice", required = false) Double maxPrice) {
        return new ResponseEntity<>(
                productService.searchProductsByCodeOrName(key, pageNumber, pageSize, sortBy, sortOrder, minPrice, maxPrice),
                HttpStatus.OK
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/products/search")
    public ResponseEntity<ProductResponse> searchProductsByCodeOrNameForAdmin(
            @RequestParam(name = "key") String key,
            @RequestParam(name = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
            @RequestParam(name = "pageSize", defaultValue = AppConstants.PAGE_SIZE_PRODUCT, required = false) Integer pageSize,
            @RequestParam(name = "sortBy", defaultValue = AppConstants.SORT_BY_PRODUCTS, required = false) String sortBy,
            @RequestParam(name = "sortOrder", defaultValue = AppConstants.SORT_ORDER_TANG, required = false) String sortOrder,
            @RequestParam(name = "minPrice", required = false) Double minPrice,
            @RequestParam(name = "maxPrice", required = false) Double maxPrice,
            @RequestParam(name = "statuses", required = false) List<RecordStatus> statuses) {
        return new ResponseEntity<>(
                productService.searchProductsByCodeOrNameForAdmin(key, pageNumber, pageSize, sortBy, sortOrder, minPrice, maxPrice, statuses),
                HttpStatus.OK
        );
    }


    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/admin/products/{productId}")
    public ResponseEntity<ProductDTO> updateProduct(@RequestBody ProductDTO product,@PathVariable Long productId){
       ProductDTO productDTO =  productService.updateProduct(product, productId);
        return  new ResponseEntity<>(productDTO, HttpStatus.OK);
    }


    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/admin/products/{productId}")
    public ResponseEntity<ProductDTO> deleteProduct(@PathVariable Long productId) {
        ProductDTO flag = productService.deleteProduct(productId);
        return  new ResponseEntity<>(flag, HttpStatus.OK);
    }

    @PutMapping("/products/{productId}/image")
    public ResponseEntity<ProductDTO> updateProductImage(@PathVariable Long productId,
                                                         @RequestParam("image")MultipartFile image)  throws IOException {
        ProductDTO updateproductDTO = productService.updateProductImage(productId, image);
        return new ResponseEntity<>(updateproductDTO, HttpStatus.OK);
    }

}
