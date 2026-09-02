package com.citypulse.city;

// Thrown when a text query (e.g. GET /api/v1/pulse?q=) matches no city at all.
// Mapped to HTTP 404 by com.citypulse.CityNotFoundExceptionMapper
public class CityNotFoundException extends RuntimeException {

    public CityNotFoundException(String message) {
        super(message);
    }
}
