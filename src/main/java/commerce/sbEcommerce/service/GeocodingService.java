package commerce.sbEcommerce.service;

public interface GeocodingService {
    Coordinates geocode(String address);

    record Coordinates(double latitude, double longitude) {
    }
}
