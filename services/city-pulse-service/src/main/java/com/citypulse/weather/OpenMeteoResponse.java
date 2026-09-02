package com.citypulse.weather;

import com.fasterxml.jackson.annotation.JsonProperty;

public record OpenMeteoResponse(
        Current current
) {
    public record Current(
            @JsonProperty("temperature_2m")
            double temperature2m,

            @JsonProperty("weather_code")
            int weatherCode,

            @JsonProperty("relative_humidity_2m")
            int relativeHumidity2m,

            @JsonProperty("wind_speed_10m")
            double windSpeed10m
    ) {
    }
}
