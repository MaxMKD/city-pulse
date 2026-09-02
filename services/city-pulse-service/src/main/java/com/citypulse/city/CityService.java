package com.citypulse.city;

import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class CityService {

    private final PhotonGeocodingProvider geocodingProvider;

    public CityService(PhotonGeocodingProvider geocodingProvider) {
        this.geocodingProvider = geocodingProvider;
    }

    public List<City> search(String query) {
        return geocodingProvider.search(query);
    }

    // Photon already ranks results by relevance, so "first match" is its best guess.
    public Optional<City> searchBestMatch(String query) {
        return search(query).stream().findFirst();
    }
}
