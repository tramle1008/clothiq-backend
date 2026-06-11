package commerce.sbEcommerce.service;

import com.fasterxml.jackson.databind.JsonNode;
import commerce.sbEcommerce.exceptioons.APIException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class NominatimGeocodingService implements GeocodingService {
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${geocoding.nominatim.base-url:https://nominatim.openstreetmap.org}")
    private String baseUrl;

    @Value("${geocoding.nominatim.user-agent:clothiq-store-branch-service}")
    private String userAgent;

    @Override
    public Coordinates geocode(String address) {
        String url = UriComponentsBuilder.fromHttpUrl(baseUrl)
                .path("/search")
                .queryParam("q", address)
                .queryParam("format", "jsonv2")
                .queryParam("limit", 1)
                .queryParam("addressdetails", 0)
                .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.USER_AGENT, userAgent);
        headers.setAccept(MediaType.parseMediaTypes(MediaType.APPLICATION_JSON_VALUE));

        JsonNode response;
        try {
            response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), JsonNode.class).getBody();
        } catch (RestClientException ex) {
            throw new APIException("Failed to geocode address: " + address);
        }

        if (response == null || !response.isArray() || response.isEmpty()) {
            throw new APIException("Unable to geocode address: " + address);
        }

        JsonNode first = response.get(0);
        double latitude = first.path("lat").asDouble(Double.NaN);
        double longitude = first.path("lon").asDouble(Double.NaN);

        if (Double.isNaN(latitude) || Double.isNaN(longitude)) {
            throw new APIException("Geocoding provider returned invalid coordinates");
        }

        return new Coordinates(latitude, longitude);
    }
}
