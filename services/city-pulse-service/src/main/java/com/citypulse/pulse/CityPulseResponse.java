package com.citypulse.pulse;

// status is "COMPLETE", "PARTIAL" (one data source unavailable) or "UNAVAILABLE" (both unavailable).
// score, weather and airQuality are null when the underlying data could not be obtained.
public record CityPulseResponse(
        CityInfo city,
        Integer score,
        String status,
        WeatherSummary weather,
        AirQualitySummary airQuality,
        ScoreBreakdown scoreBreakdown,
        ProviderStatus providers
) {
}
