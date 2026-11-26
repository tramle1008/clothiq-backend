package commerce.sbEcommerce.controller;

import commerce.sbEcommerce.model.Banner;
import commerce.sbEcommerce.model.BannerPlacement;
import commerce.sbEcommerce.repository.BannerRepository;
import commerce.sbEcommerce.service.FileService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/banners")
public class BannerController {
    private final BannerRepository banners;
    private final FileService fileService;
    @Value("${project.image}") private String imagePath;

    public BannerController(BannerRepository banners, FileService fileService) { this.banners = banners; this.fileService = fileService; }

    @GetMapping
    public List<Banner> list(@RequestParam(required = false) BannerPlacement placement) {
        return placement == null ? banners.findAll().stream()
                .sorted(java.util.Comparator.comparing(Banner::getPlacement).thenComparing(Banner::getDisplayOrder).thenComparing(Banner::getBannerId)).toList()
                : banners.findAllByPlacementOrderByDisplayOrderAscBannerIdAsc(placement);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Banner create(@RequestParam String title, @RequestParam String description,
                         @RequestParam(required = false) String linkUrl,
                         @RequestParam(required = false) Integer displayOrder,
                         @RequestParam(required = false) BannerPlacement placement,
                         @RequestParam MultipartFile image) throws IOException {
        Banner banner = new Banner();
        banner.setTitle(title);
        banner.setDescription(description);
        banner.setImageUrl("/images/" + fileService.uploadImage(imagePath, image));
        banner.setLinkUrl(linkUrl);
        banner.setDisplayOrder(displayOrder);
        banner.setPlacement(placement);
        applyDefaults(banner);
        banner.setBannerId(null);
        return banners.save(banner);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public Banner update(@PathVariable Long id, @RequestParam String title, @RequestParam String description,
                         @RequestParam(required = false) String linkUrl,
                         @RequestParam(required = false) Integer displayOrder,
                         @RequestParam(required = false) BannerPlacement placement,
                         @RequestParam(required = false) MultipartFile image) throws IOException {
        Banner banner = banners.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Banner not found"));
        banner.setTitle(title);
        banner.setDescription(description);
        if (image != null && !image.isEmpty()) banner.setImageUrl("/images/" + fileService.uploadImage(imagePath, image));
        banner.setLinkUrl(linkUrl == null || linkUrl.isBlank() ? "/products" : linkUrl);
        banner.setDisplayOrder(displayOrder == null ? 0 : displayOrder);
        banner.setPlacement(placement == null ? BannerPlacement.HERO : placement);
        return banners.save(banner);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        if (!banners.existsById(id)) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Banner not found");
        banners.deleteById(id);
    }

    private void applyDefaults(Banner banner) {
        if (banner.getLinkUrl() == null || banner.getLinkUrl().isBlank()) banner.setLinkUrl("/products");
        if (banner.getDisplayOrder() == null) banner.setDisplayOrder(0);
        if (banner.getPlacement() == null) banner.setPlacement(BannerPlacement.HERO);
    }
}
