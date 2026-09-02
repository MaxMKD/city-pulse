package com.citypulse.airquality;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.WebApplicationException;

import org.eclipse.microprofile.rest.client.inject.RestClient;

@ApplicationScoped
public class OpenMeteoAirQualityProvider {

    private static final String CURRENT_FIELDS = "pm2_5,european_aqi";

    private final OpenMeteoAirQualityClient airQualityClient;

    public OpenMeteoAirQualityProvider(@RestClient OpenMeteoAirQualityClient airQualityClient) {
        this.airQualityClient = airQualityClient;
    }

    public AirQuality fetchAirQuality(double latitude, double longitude) {
        OpenMeteoAirQualityResponse response;
        try {
            response = airQualityClient.getCurrentAirQuality(latitude, longitude, CURRENT_FIELDS);
        } catch (ProcessingException | WebApplicationException e) {
            throw new AirQualityProviderException("Open-Meteo air quality provider is unavailable", e);
        }

        OpenMeteoAirQualityResponse.Current current = response.current();

        return new AirQuality(
                current.pm25(),
                current.europeanAqi(),
                mapEuropeanAqiToStatus(current.europeanAqi())
        );
    }

    // Official European AQI bands, as documented by Open-Meteo.
    static String mapEuropeanAqiToStatus(double europeanAqi) {
        if (europeanAqi <= 20) {
            return "GOOD";
        }
        if (europeanAqi <= 40) {
            return "FAIR";
        }
        if (europeanAqi <= 60) {
            return "MODERATE";
        }
        if (europeanAqi <= 80) {
            return "POOR";
        }
        if (europeanAqi <= 100) {
            return "VERY_POOR";
        }
        return "EXTREMELY_POOR";
    }
}
