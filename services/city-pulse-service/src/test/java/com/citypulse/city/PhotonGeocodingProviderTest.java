package com.citypulse.city;

import jakarta.ws.rs.ProcessingException;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PhotonGeocodingProviderTest {

    @Test
    void mapsPhotonFeaturesIntoCitiesWithCorrectCoordinateOrder() {
        // GeoJSON coordinates are [longitude, latitude] — this must come out as (52.52, 13.405).
        PhotonClient fakeClient = (query, limit, language) -> new PhotonResponse(List.of(
                new PhotonResponse.Feature(
                        new PhotonResponse.Geometry(List.of(13.405, 52.52)),
                        new PhotonResponse.Properties("Berlin", "Germany", "DE")
                )
        ));

        PhotonGeocodingProvider provider = new PhotonGeocodingProvider(fakeClient);

        List<City> cities = provider.search("berlin");

        assertEquals(List.of(new City("Berlin", "Germany", "DE", 52.52, 13.405)), cities);
    }

    @Test
    void defaultsMissingCountryAndCountryCodeToEmptyString() {
        PhotonClient fakeClient = (query, limit, language) -> new PhotonResponse(List.of(
                new PhotonResponse.Feature(
                        new PhotonResponse.Geometry(List.of(13.405, 52.52)),
                        new PhotonResponse.Properties("Berlin", null, null)
                )
        ));

        PhotonGeocodingProvider provider = new PhotonGeocodingProvider(fakeClient);

        City city = provider.search("berlin").get(0);

        assertEquals("", city.country());
        assertEquals("", city.countryCode());
    }

    @Test
    void skipsFeaturesMissingNameOrCoordinates() {
        PhotonClient fakeClient = (query, limit, language) -> new PhotonResponse(List.of(
                new PhotonResponse.Feature(
                        new PhotonResponse.Geometry(List.of(13.405, 52.52)),
                        new PhotonResponse.Properties(null, "Germany", "DE")
                ),
                new PhotonResponse.Feature(
                        new PhotonResponse.Geometry(List.of()),
                        new PhotonResponse.Properties("Nowhere", "Germany", "DE")
                )
        ));

        PhotonGeocodingProvider provider = new PhotonGeocodingProvider(fakeClient);

        assertTrue(provider.search("berlin").isEmpty());
    }

    @Test
    void searchWrapsProviderFailureAsCityGeocodingException() {
        PhotonClient failingClient = (query, limit, language) -> {
            throw new ProcessingException("connection refused");
        };

        PhotonGeocodingProvider provider = new PhotonGeocodingProvider(failingClient);

        assertThrows(CityGeocodingException.class, () -> provider.search("berlin"));
    }
}
