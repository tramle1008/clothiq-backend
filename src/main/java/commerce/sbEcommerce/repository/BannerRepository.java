package commerce.sbEcommerce.repository;

import commerce.sbEcommerce.model.Banner;
import commerce.sbEcommerce.model.BannerPlacement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BannerRepository extends JpaRepository<Banner, Long> {
    List<Banner> findAllByPlacementOrderByDisplayOrderAscBannerIdAsc(BannerPlacement placement);
}
