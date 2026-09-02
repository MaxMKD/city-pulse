package com.citypulse.pulse;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

// The only public endpoint in this service. Search text in, CityPulse score out — one call.
@Path("/api/v1/pulse")
@Produces(MediaType.APPLICATION_JSON)
public class CityPulseResource {

    private final CityPulseService cityPulseService;

    public CityPulseResource(CityPulseService cityPulseService) {
        this.cityPulseService = cityPulseService;
    }

    @GET
    public CityPulseResponse getPulse(@QueryParam("q") String query) {
        if (query == null || query.isBlank()) {
            throw new IllegalArgumentException("Query parameter 'q' is required, e.g. ?q=berlin");
        }

        return cityPulseService.getPulseForQuery(query);
    }
}
