package com.citypulse.weather;

public record Weather(
        double temperatureCelsius,
        String condition,
        int humidityPercent,
        double windSpeedKmh
) {
}
