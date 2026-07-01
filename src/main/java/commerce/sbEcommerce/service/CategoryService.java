package commerce.sbEcommerce.service;

import commerce.sbEcommerce.model.RecordStatus;
import commerce.sbEcommerce.model.Category;
import commerce.sbEcommerce.payload.CategoryDTO;
import commerce.sbEcommerce.payload.CategoryResponse;
import org.springframework.stereotype.Service;

import java.util.List;

public interface CategoryService {
    CategoryResponse getAllCategories(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder);
    CategoryResponse getAllCategoriesForAdmin(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder, List<RecordStatus> statuses);
    CategoryDTO createCategory(CategoryDTO categoryDTO);
    List<CategoryDTO> createbatchCategories(List<CategoryDTO> categoryDTOs);
    String deleteCategory(Long categoryid);
    CategoryDTO updateCategory(CategoryDTO category, Long categoryId);
    List<CategoryDTO> searchCategories(String keyword, Integer limit);
    List<CategoryDTO> searchCategoriesForAdmin(String keyword, Integer limit, List<RecordStatus> statuses);
}
