package commerce.sbEcommerce.repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import commerce.sbEcommerce.model.Category;
import commerce.sbEcommerce.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;


public interface ProductRepository extends JpaRepository<Product, Long> , JpaSpecificationExecutor<Product> {
    Page<Product> findByCategory(Category category, Pageable pageable);
    Page<Product> findByProductNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
            String productName,
            String description,
            Pageable pageable
    );

    Page<Product> findByProductCodeContainingIgnoreCaseOrProductNameContainingIgnoreCase(
            String productCode,
            String productName,
            Pageable pageable
    );
}
