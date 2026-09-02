package com.citypulse.airquality;

public record AirQuality(
        double pm25,
        double europeanAqi,
        String status
) {
}
