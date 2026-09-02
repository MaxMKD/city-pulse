package com.citypulse.airquality;

// Mapped to HTTP 502 by com.citypulse.AirQualityProviderExceptionMapper
public class AirQualityProviderException extends RuntimeException {

    public AirQualityProviderException(String message, Throwable cause) {
        super(message, cause);
    }
}
