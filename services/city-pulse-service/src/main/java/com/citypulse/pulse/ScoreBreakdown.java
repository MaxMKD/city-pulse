package com.citypulse.pulse;

// weather/airQuality are null when that data source was unavailable.
public record ScoreBreakdown(
        Integer weather,
        Integer airQuality
) {
}
