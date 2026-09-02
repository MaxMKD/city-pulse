package com.citypulse.weather;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.WebApplicationException;

import org.eclipse.microprofile.rest.client.inject.RestClient;

@ApplicationScoped
public class OpenMeteoWeatherProvider {

    private static final String CURRENT_FIELDS = "temperature_2m,weather_code,relative_humidity_2m,wind_speed_10m";

    private final OpenMeteoClient openMeteoClient;

    public OpenMeteoWeatherProvider(@RestClient OpenMeteoClient openMeteoClient) {
        this.openMeteoClient = openMeteoClient;
    }

    public Weather fetchWeather(double latitude, double longitude) {
        OpenMeteoResponse response;
        try {
            response = openMeteoClient.getCurrentWeather(latitude, longitude, CURRENT_FIELDS);
        } catch (ProcessingException | WebApplicationException e) {
            throw new WeatherProviderException("Open-Meteo weather provider is unavailable", e);
        }

        OpenMeteoResponse.Current current = response.current();

        return new Weather(
                current.temperature2m(),
                mapWeatherCode(current.weatherCode()),
                current.relativeHumidity2m(),
                current.windSpeed10m()
        );
    }

    // WMO weather interpretation codes, as used by Open-Meteo.
    static String mapWeatherCode(int weatherCode) {
        return switch (weatherCode) {
            case 0 -> "CLEAR";
            case 1, 2 -> "PARTLY_CLOUDY";
            case 3 -> "CLOUDY";
            case 45, 48 -> "FOG";
            case 51, 53, 55, 56, 57 -> "DRIZZLE";
            case 61, 63, 65, 66, 67 -> "RAIN";
            case 71, 73, 75, 77, 85, 86 -> "SNOW";
            case 80, 81, 82 -> "SHOWERS";
            case 95, 96, 99 -> "THUNDERSTORM";
            default -> "UNKNOWN";
        };
    }
}
