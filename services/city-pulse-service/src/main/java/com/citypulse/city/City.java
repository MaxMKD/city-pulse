package com.citypulse.city;

public record City(
        String name,
        String country,
        String countryCode,
        double latitude,
        double longitude
) {
}
