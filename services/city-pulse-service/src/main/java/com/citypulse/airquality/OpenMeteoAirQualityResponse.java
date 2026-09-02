package com.citypulse.airquality;

import com.fasterxml.jackson.annotation.JsonProperty;

public record OpenMeteoAirQualityResponse(
        Current current
) {
    public record Current(
            @JsonProperty("pm2_5")
            double pm25,

            @JsonProperty("european_aqi")
            double europeanAqi
    ) {
    }
}
