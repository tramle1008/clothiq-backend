package commerce.sbEcommerce.repository;

import commerce.sbEcommerce.model.Product;
import commerce.sbEcommerce.model.RecordStatus;
import commerce.sbEcommerce.payload.CategoryProductCount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    @Query("""
        SELECT
            p.category.categoryId AS categoryId,
            COUNT(p) AS productCount
        FROM Product p
        WHERE p.category.categoryId IN :categoryIds
          AND p.status = :status
        GROUP BY p.category.categoryId
    """)
    List<CategoryProductCount> countProductsByCategory(
            @Param("categoryIds") List<Long> categoryIds,
            @Param("status") RecordStatus status
    );
}
