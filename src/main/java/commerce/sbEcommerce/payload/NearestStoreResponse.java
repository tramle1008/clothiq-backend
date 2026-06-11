package commerce.sbEcommerce.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NearestStoreResponse {
    private String inputAddress;
    private LocationDTO userLocation;
    private NearestStoreDTO nearestStore;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LocationDTO {
        private Double lat;
        private Double lng;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NearestStoreDTO {
        private Long id;
        private String branchName;
        private String address;
        private Double latitude;
        private Double longitude;
        private Double distanceKm;
    }
}
