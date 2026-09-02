package com.citypulse.airquality;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class AirQualityService {

    private final OpenMeteoAirQualityProvider airQualityProvider;

    public AirQualityService(OpenMeteoAirQualityProvider airQualityProvider) {
        this.airQualityProvider = airQualityProvider;
    }

    public AirQuality getAirQuality(double latitude, double longitude) {
        return airQualityProvider.fetchAirQuality(latitude, longitude);
    }
}
