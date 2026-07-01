package commerce.sbEcommerce.service;

import commerce.sbEcommerce.exceptioons.APIException;
import commerce.sbEcommerce.exceptioons.ResourceNotFoundException;
import commerce.sbEcommerce.model.Category;
import commerce.sbEcommerce.model.RecordStatus;
import commerce.sbEcommerce.payload.CategoryDTO;
import commerce.sbEcommerce.payload.CategoryResponse;
import commerce.sbEcommerce.repository.CategoryRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CategoryServiceIml implements CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public CategoryResponse getAllCategories(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        return getCategories(pageNumber, pageSize, sortBy, sortOrder, List.of(RecordStatus.ACTIVE));
    }

    @Override
    public CategoryResponse getAllCategoriesForAdmin(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder, List<RecordStatus> statuses) {
        return getCategories(pageNumber, pageSize, sortBy, sortOrder, resolveStatuses(statuses, true));
    }

    @Override
    public CategoryDTO createCategory(CategoryDTO categoryDTO) {
        List<Category> existingCategories = categoryRepository.findByCategoryNameIgnoreCase(categoryDTO.getCategoryName());
        boolean hasConflict = existingCategories.stream()
                .anyMatch(category -> category.getStatus() != RecordStatus.DELETED);

        if (hasConflict) {
            throw new APIException("Category with name '" + categoryDTO.getCategoryName() + "' already exists.");
        }

        Category category = modelMapper.map(categoryDTO, Category.class);
        if (category.getStatus() == null) {
            category.setStatus(RecordStatus.ACTIVE);
        }

        Category saveCategory = categoryRepository.save(category);
        return modelMapper.map(saveCategory, CategoryDTO.class);
    }

    @Override
    public List<CategoryDTO> createbatchCategories(List<CategoryDTO> categoryDTOs) {
        List<CategoryDTO> savedCategories = new ArrayList<>();
        for (CategoryDTO categoryDTO : categoryDTOs) {
            Category category = modelMapper.map(categoryDTO, Category.class);
            if (category.getStatus() == null) {
                category.setStatus(RecordStatus.ACTIVE);
            }
            Category saved = categoryRepository.save(category);
            savedCategories.add(modelMapper.map(saved, CategoryDTO.class));
        }
        return savedCategories;
    }

    @Override
    public String deleteCategory(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "categoryId", categoryId));
        category.setStatus(RecordStatus.DELETED);
        categoryRepository.save(category);
        return "Category id " + categoryId + " deleted successful!";
    }

    @Override
    public CategoryDTO updateCategory(CategoryDTO categoryDTO, Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "categoryId", categoryId));

        if (categoryDTO.getCategoryName() != null) {
            category.setCategoryName(categoryDTO.getCategoryName());
        }
        if (categoryDTO.getStatus() != null) {
            category.setStatus(categoryDTO.getStatus());
        }

        Category saveCategory = categoryRepository.save(category);
        return modelMapper.map(saveCategory, CategoryDTO.class);
    }

    @Override
    public List<CategoryDTO> searchCategories(String keyword, Integer limit) {
        return searchCategoriesByStatuses(keyword, limit, List.of(RecordStatus.ACTIVE));
    }

    @Override
    public List<CategoryDTO> searchCategoriesForAdmin(String keyword, Integer limit, List<RecordStatus> statuses) {
        return searchCategoriesByStatuses(keyword, limit, resolveStatuses(statuses, true));
    }

    private CategoryResponse getCategories(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder, List<RecordStatus> statuses) {
        Sort sortByOrder = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sortByOrder);
        Specification<Category> spec = (root, query, criteriaBuilder) -> root.get("status").in(statuses);
        Page<Category> categoryPage = categoryRepository.findAll(spec, pageDetails);

        List<CategoryDTO> categoryDTOS = categoryPage.getContent().stream()
                .map(category -> modelMapper.map(category, CategoryDTO.class))
                .collect(Collectors.toList());

        CategoryResponse categoryResponse = new CategoryResponse();
        categoryResponse.setContent(categoryDTOS);
        categoryResponse.setPageNumber(categoryPage.getNumber());
        categoryResponse.setPageSize(categoryPage.getSize());
        categoryResponse.setTotalElements(categoryPage.getTotalElements());
        categoryResponse.setTotalPages(categoryPage.getTotalPages());
        categoryResponse.setLastPage(categoryPage.isLast());
        return categoryResponse;
    }

    private List<CategoryDTO> searchCategoriesByStatuses(String keyword, Integer limit, List<RecordStatus> statuses) {
        String normalizedKeyword = normalizeKeyword(keyword);
        if (normalizedKeyword.isEmpty()) {
            return new ArrayList<>();
        }

        int resultLimit = (limit == null || limit <= 0) ? 10 : limit;
        Set<RecordStatus> allowedStatuses = EnumSet.copyOf(statuses);

        return categoryRepository.findAll().stream()
                .filter(category -> allowedStatuses.contains(category.getStatus()))
                .map(category -> new CategoryMatch(category, calculateScore(normalizedKeyword, category.getCategoryName())))
                .filter(match -> match.score() > 0)
                .sorted(Comparator
                        .comparingInt(CategoryMatch::score).reversed()
                        .thenComparingInt(match -> levenshtein(normalizedKeyword, normalizeKeyword(match.category().getCategoryName())))
                        .thenComparing(match -> match.category().getCategoryName(), String.CASE_INSENSITIVE_ORDER))
                .limit(resultLimit)
                .map(match -> modelMapper.map(match.category(), CategoryDTO.class))
                .collect(Collectors.toList());
    }

    private List<RecordStatus> resolveStatuses(List<RecordStatus> statuses, boolean includeDeletedDefault) {
        if (statuses == null || statuses.isEmpty()) {
            return includeDeletedDefault
                    ? List.of(RecordStatus.ACTIVE, RecordStatus.INACTIVE, RecordStatus.DELETED)
                    : List.of(RecordStatus.ACTIVE);
        }
        return statuses;
    }

    private int calculateScore(String normalizedKeyword, String categoryName) {
        String normalizedCategoryName = normalizeKeyword(categoryName);
        if (normalizedCategoryName.isEmpty()) {
            return 0;
        }

        if (normalizedCategoryName.equals(normalizedKeyword)) {
            return 1000;
        }

        if (normalizedCategoryName.startsWith(normalizedKeyword)) {
            return 900 - (normalizedCategoryName.length() - normalizedKeyword.length());
        }

        if (normalizedCategoryName.contains(normalizedKeyword)) {
            return 750 - normalizedCategoryName.indexOf(normalizedKeyword);
        }

        boolean allTokensPresent = true;
        for (String token : normalizedKeyword.split(" ")) {
            if (!normalizedCategoryName.contains(token)) {
                allTokensPresent = false;
                break;
            }
        }
        if (allTokensPresent) {
            return 600;
        }

        int distance = levenshtein(normalizedKeyword, normalizedCategoryName);
        int maxDistance = Math.max(1, normalizedKeyword.length() / 3);
        if (distance <= maxDistance) {
            return 500 - distance * 10;
        }

        return 0;
    }

    private String normalizeKeyword(String value) {
        if (value == null) {
            return "";
        }

        return Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replace('đ', 'd')
                .replace('Đ', 'D')
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9\\s]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private int levenshtein(String left, String right) {
        int[][] dp = new int[left.length() + 1][right.length() + 1];

        for (int i = 0; i <= left.length(); i++) {
            dp[i][0] = i;
        }
        for (int j = 0; j <= right.length(); j++) {
            dp[0][j] = j;
        }

        for (int i = 1; i <= left.length(); i++) {
            for (int j = 1; j <= right.length(); j++) {
                int cost = left.charAt(i - 1) == right.charAt(j - 1) ? 0 : 1;
                dp[i][j] = Math.min(
                        Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1),
                        dp[i - 1][j - 1] + cost
                );
            }
        }

        return dp[left.length()][right.length()];
    }

    private record CategoryMatch(Category category, int score) {
    }
}
