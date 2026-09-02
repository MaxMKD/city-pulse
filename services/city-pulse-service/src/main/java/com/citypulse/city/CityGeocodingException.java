package com.citypulse.city;

// Mapped to HTTP 502 by com.citypulse.CityGeocodingExceptionMapper
public class CityGeocodingException extends RuntimeException {

    public CityGeocodingException(String message, Throwable cause) {
        super(message, cause);
    }
}
