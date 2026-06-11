package commerce.sbEcommerce.service;

import commerce.sbEcommerce.payload.ProductDTO;
import commerce.sbEcommerce.payload.ProductResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface ProductService {


    ProductDTO addProduct(Long categoryId, ProductDTO productDTO, MultipartFile image) throws IOException;

    ProductResponse getAllProduct(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder, String key, Integer categoryId);

    ProductResponse getProductByKey(String key, Integer pageNumber, Integer pageSize, String sortBy, String sortOrder);

    ProductResponse searchProductsByCodeOrName(String key, Integer pageNumber, Integer pageSize, String sortBy, String sortOrder);


    ProductResponse getProductByCategory(Long categoryId, Integer pageNumber, Integer pageSize, String sortBy, String sortOrder);


    ProductDTO updateProduct(ProductDTO product, Long productId);

    ProductDTO deleteProduct(Long productId);

    ProductDTO updateProductImage(Long productId, MultipartFile image) throws IOException;

    ProductDTO addProductDefault(Long categoryId, ProductDTO productDTO);

    ProductDTO addProduct_Image(Long categoryId, ProductDTO productDTO, MultipartFile imageFile) throws IOException;

    ProductDTO getProductById(Long productId);
}
