package commerce.sbEcommerce.repository;

//Crud

import commerce.sbEcommerce.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long>, JpaSpecificationExecutor<Category> {

    Category findByCategoryName(String category);

    List<Category> findByCategoryNameIgnoreCase(String categoryName);
}
