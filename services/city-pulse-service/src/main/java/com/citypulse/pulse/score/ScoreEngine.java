package com.citypulse.pulse.score;

import jakarta.enterprise.context.ApplicationScoped;

// Pure business logic: no HTTP calls, no knowledge of Open-Meteo or any provider.
@ApplicationScoped
public class ScoreEngine {

    private static final double IDEAL_TEMPERATURE_MIN = 18.0;
    private static final double IDEAL_TEMPERATURE_MAX = 24.0;
    private static final int TEMPERATURE_PENALTY_PER_DEGREE = 3;

    private static final double WEATHER_WEIGHT = 0.60;
    private static final double AIR_QUALITY_WEIGHT = 0.40;

    public int scoreWeather(double temperatureCelsius, String condition) {
        int temperatureScore = scoreTemperature(temperatureCelsius);
        int conditionPenalty = conditionPenalty(condition);
        return clamp(temperatureScore - conditionPenalty);
    }

    public int scoreAirQuality(double europeanAqi) {
        if (europeanAqi <= 20) {
            return 100;
        }
        if (europeanAqi <= 40) {
            return 90;
        }
        if (europeanAqi <= 60) {
            return 75;
        }
        if (europeanAqi <= 80) {
            return 60;
        }
        if (europeanAqi <= 100) {
            return 40;
        }
        return 20;
    }

    public int overallScore(int weatherScore, int airQualityScore) {
        double weighted = weatherScore * WEATHER_WEIGHT + airQualityScore * AIR_QUALITY_WEIGHT;
        return clamp((int) Math.round(weighted));
    }

    private int scoreTemperature(double temperatureCelsius) {
        if (temperatureCelsius >= IDEAL_TEMPERATURE_MIN && temperatureCelsius <= IDEAL_TEMPERATURE_MAX) {
            return 100;
        }
        double degreesOutsideIdealRange = temperatureCelsius < IDEAL_TEMPERATURE_MIN
                ? IDEAL_TEMPERATURE_MIN - temperatureCelsius
                : temperatureCelsius - IDEAL_TEMPERATURE_MAX;

        return clamp(100 - (int) Math.round(degreesOutsideIdealRange * TEMPERATURE_PENALTY_PER_DEGREE));
    }

    private int conditionPenalty(String condition) {
        return switch (condition) {
            case "CLEAR" -> 0;
            case "PARTLY_CLOUDY" -> 5;
            case "CLOUDY" -> 10;
            case "FOG" -> 15;
            case "DRIZZLE" -> 15;
            case "RAIN" -> 20;
            case "SHOWERS" -> 20;
            case "SNOW" -> 20;
            case "THUNDERSTORM" -> 35;
            default -> 10;
        };
    }

    private static int clamp(int score) {
        return Math.max(0, Math.min(100, score));
    }
}
