package commerce.sbEcommerce.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "store_branch")
public class StoreBranch {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(name = "branch_name", nullable = false)
    private String branchName;

    @NotBlank
    @Column(nullable = false, columnDefinition = "TEXT")
    private String address;

    @Column(name = "map_url", columnDefinition = "TEXT")
    private String mapUrl;

    @Column(name = "embed_url", columnDefinition = "TEXT")
    private String embedUrl;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;
}
