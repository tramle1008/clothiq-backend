package commerce.sbEcommerce.repository;

import commerce.sbEcommerce.model.Ward;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WardRepository extends JpaRepository<Ward, Long> {
    List<Ward> findByProvinceProvinceId(Long provinceId);
}
