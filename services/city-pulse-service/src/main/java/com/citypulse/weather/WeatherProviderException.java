package com.citypulse.weather;

// Mapped to HTTP 502 by com.citypulse.WeatherProviderExceptionMapper
public class WeatherProviderException extends RuntimeException {

    public WeatherProviderException(String message, Throwable cause) {
        super(message, cause);
    }
}
