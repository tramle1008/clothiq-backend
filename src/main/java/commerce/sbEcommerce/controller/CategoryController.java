package commerce.sbEcommerce.controller;

import commerce.sbEcommerce.config.AppConstants;
import commerce.sbEcommerce.model.RecordStatus;
import commerce.sbEcommerce.payload.CategoryDTO;
import commerce.sbEcommerce.payload.CategoryResponse;
import commerce.sbEcommerce.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;


    @GetMapping()
    public ResponseEntity<CategoryResponse> getCategoryList(
            @RequestParam(name = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER) Integer pageNumber,
            @RequestParam(name = "pageSize", defaultValue = AppConstants.PAGE_SIZE_ALL) Integer pageSize,
            @RequestParam(name = "sortBy", defaultValue = AppConstants.SORT_BY_CATEGORYID) String sortBy,
            @RequestParam(name = "sortOrder", defaultValue = AppConstants.SORT_ORDER_TANG) String sortOrder
    ) {
        CategoryResponse categoryList = categoryService.getAllCategories(pageNumber, pageSize, sortBy, sortOrder);
        return new ResponseEntity<>(categoryList, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin")
    public ResponseEntity<CategoryResponse> getCategoryListForAdmin(
            @RequestParam(name = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER) Integer pageNumber,
            @RequestParam(name = "pageSize", defaultValue = AppConstants.PAGE_SIZE_ALL) Integer pageSize,
            @RequestParam(name = "sortBy", defaultValue = AppConstants.SORT_BY_CATEGORYID) String sortBy,
            @RequestParam(name = "sortOrder", defaultValue = AppConstants.SORT_ORDER_TANG) String sortOrder,
            @RequestParam(name = "statuses", required = false) List<RecordStatus> statuses
    ) {
        CategoryResponse categoryList = categoryService.getAllCategoriesForAdmin(pageNumber, pageSize, sortBy, sortOrder, statuses);
        return new ResponseEntity<>(categoryList, HttpStatus.OK);
    }

    @GetMapping("/api/public/categories/search")
    public ResponseEntity<List<CategoryDTO>> searchCategories(
            @RequestParam("keyword") String keyword,
            @RequestParam(name = "limit", defaultValue = "10") Integer limit
    ) {
        List<CategoryDTO> categories = categoryService.searchCategories(keyword, limit);
        return new ResponseEntity<>(categories, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/search")
    public ResponseEntity<List<CategoryDTO>> searchCategoriesForAdmin(
            @RequestParam("keyword") String keyword,
            @RequestParam(name = "limit", defaultValue = "10") Integer limit,
            @RequestParam(name = "statuses", required = false) List<RecordStatus> statuses
    ) {
        List<CategoryDTO> categories = categoryService.searchCategoriesForAdmin(keyword, limit, statuses);
        return new ResponseEntity<>(categories, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/categories")
    public ResponseEntity<CategoryDTO> createCategory(@Valid @RequestBody CategoryDTO categoryDTO) {
        CategoryDTO saveCategoryDTO=  categoryService.createCategory(categoryDTO);
        return new ResponseEntity<>(saveCategoryDTO, HttpStatus.CREATED);
    }

//    @PreAuthorize("hasRole('ADMIN')")
//    @PostMapping("/api/categories/batch")
//    public ResponseEntity<List<CategoryDTO>> createCategories(@Valid @RequestBody List<CategoryDTO> categoryDTOs) {
//        List<CategoryDTO> savedCategories = categoryService.createbatchCategories(categoryDTOs);
//        return new ResponseEntity<>(savedCategories, HttpStatus.CREATED);
//    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{categoryid}")
    public ResponseEntity<String> deleteCategory(@PathVariable Long categoryid) {
            String flag = categoryService.deleteCategory(categoryid);
            return  new ResponseEntity<>(flag, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{categoryid}")
    public ResponseEntity<CategoryDTO> updateCategory(@Valid @RequestBody CategoryDTO categoryDTO,
                                                      @PathVariable("categoryid") Long categoryId) {
        CategoryDTO updatedCategoryDTO = categoryService.updateCategory(categoryDTO, categoryId);
        return new ResponseEntity<>(updatedCategoryDTO, HttpStatus.OK);
    }



}
