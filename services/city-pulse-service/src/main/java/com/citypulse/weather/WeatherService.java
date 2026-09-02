package com.citypulse.weather;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class WeatherService {

    private final OpenMeteoWeatherProvider weatherProvider;

    public WeatherService(OpenMeteoWeatherProvider weatherProvider) {
        this.weatherProvider = weatherProvider;
    }

    public Weather getWeather(double latitude, double longitude) {
        return weatherProvider.fetchWeather(latitude, longitude);
    }
}
