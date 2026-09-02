package com.citypulse.pulse;

// Values are "AVAILABLE" or "UNAVAILABLE".
public record ProviderStatus(
        String weather,
        String airQuality
) {
}
