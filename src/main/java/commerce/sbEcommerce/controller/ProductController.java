package commerce.sbEcommerce.controller;

import commerce.sbEcommerce.config.AppConstants;
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

@RestController
@RequestMapping("/api")
public class ProductController {
    @Autowired
    private ProductService productService;

//    @PostMapping("/admin/categories/{categoryId}/product")
    @PostMapping(
        value = "/admin/categories/{categoryId}/product",
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<ProductDTO> addProduct(
            @PathVariable Long categoryId,
            @RequestPart("product") ProductDTO productDTO,
            @RequestPart(value = "image", required = false) MultipartFile image
    ) throws IOException {
        ProductDTO addProduct = productService.addProduct(categoryId, productDTO, image);
        return  new ResponseEntity<>(addProduct, HttpStatus.CREATED);
    }


//thu lan 2
    @PostMapping("/admin/categories/{categoryId}/productImage")
    public ResponseEntity<ProductDTO> addProduct_Image(
            @PathVariable Long categoryId,
            @RequestPart("product") ProductDTO productDTO,
            @RequestPart(value = "imageFile", required = false) MultipartFile imageFile
    ) throws IOException {
        ProductDTO createdProduct = productService.addProduct_Image(categoryId, productDTO, imageFile);
        return ResponseEntity.ok(createdProduct);
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


    @GetMapping("/public/products")
    public ResponseEntity<ProductResponse> getAllProduct(
            @RequestParam(name =  "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
            @RequestParam(name =  "pageSize", defaultValue = AppConstants.PAGE_SIZE_PRODUCT, required = false ) Integer pageSize,
            @RequestParam(name =  "sortBy", defaultValue = AppConstants.SORT_BY_PRODUCTS, required = false ) String sortBy ,
            @RequestParam(name =  "sortOrder" , defaultValue = AppConstants.SORT_ORDER_TANG, required = false ) String sortOrder,
            @RequestParam(name = "key", required = false) String key,
            @RequestParam(name = "categoryId", required = false) Integer categoryId){
        return new ResponseEntity<>(productService.getAllProduct(pageNumber, pageSize,sortBy,sortOrder ,key, categoryId ),HttpStatus.OK);
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
            @RequestParam(name = "sortOrder", defaultValue = AppConstants.SORT_ORDER_TANG, required = false) String sortOrder) {
        return new ResponseEntity<>(
                productService.searchProductsByCodeOrName(key, pageNumber, pageSize, sortBy, sortOrder),
                HttpStatus.OK
        );
    }


    @PutMapping("/admin/products/{productId}")
    public ResponseEntity<ProductDTO> updateProduct(@RequestBody ProductDTO product,@PathVariable Long productId){
       ProductDTO productDTO =  productService.updateProduct(product, productId);
        return  new ResponseEntity<>(productDTO, HttpStatus.OK);
    }


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
