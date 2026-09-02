package com.citypulse.city;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.WebApplicationException;

import java.util.List;

import org.eclipse.microprofile.rest.client.inject.RestClient;

@ApplicationScoped
public class PhotonGeocodingProvider {

    private static final int RESULT_LIMIT = 10;
    private static final String LANGUAGE = "en";

    private final PhotonClient photonClient;

    public PhotonGeocodingProvider(@RestClient PhotonClient photonClient) {
        this.photonClient = photonClient;
    }

    public List<City> search(String query) {
        PhotonResponse response;
        try {
            response = photonClient.search(query, RESULT_LIMIT, LANGUAGE);
        } catch (ProcessingException | WebApplicationException e) {
            throw new CityGeocodingException("Photon geocoding provider is unavailable", e);
        }

        return response.features().stream()
                .filter(PhotonGeocodingProvider::hasNameAndCoordinates)
                .map(PhotonGeocodingProvider::toCity)
                .toList();
    }

    private static boolean hasNameAndCoordinates(PhotonResponse.Feature feature) {
        return feature.properties() != null
                && feature.properties().name() != null
                && feature.geometry() != null
                && feature.geometry().coordinates() != null
                && feature.geometry().coordinates().size() == 2;
    }

    private static City toCity(PhotonResponse.Feature feature) {
        // GeoJSON coordinate order is [longitude, latitude] — the reverse of our (lat, lon) convention.
        double longitude = feature.geometry().coordinates().get(0);
        double latitude = feature.geometry().coordinates().get(1);

        PhotonResponse.Properties properties = feature.properties();

        return new City(
                properties.name(),
                properties.country() != null ? properties.country() : "",
                properties.countryCode() != null ? properties.countryCode() : "",
                latitude,
                longitude
        );
    }
}
