package com.citypulse.airquality;

import jakarta.ws.rs.ProcessingException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OpenMeteoAirQualityProviderTest {

    @ParameterizedTest
    @CsvSource({
            "0, GOOD",
            "20, GOOD",
            "21, FAIR",
            "40, FAIR",
            "41, MODERATE",
            "60, MODERATE",
            "61, POOR",
            "80, POOR",
            "81, VERY_POOR",
            "100, VERY_POOR",
            "101, EXTREMELY_POOR"
    })
    void mapsEuropeanAqiToStatus(double europeanAqi, String expectedStatus) {
        assertEquals(expectedStatus, OpenMeteoAirQualityProvider.mapEuropeanAqiToStatus(europeanAqi));
    }

    @Test
    void fetchAirQualityTranslatesProviderResponseIntoDomainModel() {
        OpenMeteoAirQualityClient fakeClient = (latitude, longitude, current) ->
                new OpenMeteoAirQualityResponse(new OpenMeteoAirQualityResponse.Current(8.2, 24.0));

        OpenMeteoAirQualityProvider provider = new OpenMeteoAirQualityProvider(fakeClient);

        AirQuality airQuality = provider.fetchAirQuality(52.52, 13.405);

        assertEquals(new AirQuality(8.2, 24.0, "FAIR"), airQuality);
    }

    @Test
    void fetchAirQualityWrapsProviderFailureAsAirQualityProviderException() {
        OpenMeteoAirQualityClient failingClient = (latitude, longitude, current) -> {
            throw new ProcessingException("connection refused");
        };

        OpenMeteoAirQualityProvider provider = new OpenMeteoAirQualityProvider(failingClient);

        assertThrows(AirQualityProviderException.class, () -> provider.fetchAirQuality(52.52, 13.405));
    }
}
