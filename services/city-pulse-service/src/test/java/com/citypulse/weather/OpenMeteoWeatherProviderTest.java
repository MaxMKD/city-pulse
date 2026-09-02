package com.citypulse.weather;

import jakarta.ws.rs.ProcessingException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OpenMeteoWeatherProviderTest {

    @ParameterizedTest
    @CsvSource({
            "0, CLEAR",
            "1, PARTLY_CLOUDY",
            "2, PARTLY_CLOUDY",
            "3, CLOUDY",
            "45, FOG",
            "51, DRIZZLE",
            "61, RAIN",
            "71, SNOW",
            "80, SHOWERS",
            "95, THUNDERSTORM",
            "1000, UNKNOWN"
    })
    void mapsWeatherCodeToCondition(int weatherCode, String expectedCondition) {
        assertEquals(expectedCondition, OpenMeteoWeatherProvider.mapWeatherCode(weatherCode));
    }

    @Test
    void fetchWeatherTranslatesProviderResponseIntoDomainModel() {
        OpenMeteoClient fakeClient = (latitude, longitude, current) ->
                new OpenMeteoResponse(new OpenMeteoResponse.Current(21.4, 3, 64, 13.2));

        OpenMeteoWeatherProvider provider = new OpenMeteoWeatherProvider(fakeClient);

        Weather weather = provider.fetchWeather(52.52, 13.405);

        assertEquals(new Weather(21.4, "CLOUDY", 64, 13.2), weather);
    }

    @Test
    void fetchWeatherWrapsProviderFailureAsWeatherProviderException() {
        OpenMeteoClient failingClient = (latitude, longitude, current) -> {
            throw new ProcessingException("connection refused");
        };

        OpenMeteoWeatherProvider provider = new OpenMeteoWeatherProvider(failingClient);

        assertThrows(WeatherProviderException.class, () -> provider.fetchWeather(52.52, 13.405));
    }
}
