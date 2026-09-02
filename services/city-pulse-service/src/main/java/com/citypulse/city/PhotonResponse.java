package com.citypulse.city;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

// Shape of Photon's GeoJSON response (https://photon.komoot.io).
public record PhotonResponse(
        List<Feature> features
) {
    public record Feature(
            Geometry geometry,
            Properties properties
    ) {
    }

    public record Geometry(
            // GeoJSON order: [longitude, latitude] — the reverse of this project's (lat, lon) convention.
            List<Double> coordinates
    ) {
    }

    public record Properties(
            String name,
            String country,

            @JsonProperty("countrycode")
            String countryCode
    ) {
    }
}
