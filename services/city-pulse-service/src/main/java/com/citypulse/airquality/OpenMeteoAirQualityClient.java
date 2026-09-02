package com.citypulse.airquality;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.ServerErrorException;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

// configKey "open-meteo-air-quality" ties this to the quarkus.rest-client.open-meteo-air-quality.* properties
@RegisterRestClient(configKey = "open-meteo-air-quality")
@Path("/v1/air-quality")
@Produces(MediaType.APPLICATION_JSON)
public interface OpenMeteoAirQualityClient {

    // Retries only transient failures (network issues, 5xx); 4xx client errors are never retried.
    @GET
    @Retry(maxRetries = 2, delay = 200, jitter = 100, retryOn = {ProcessingException.class, ServerErrorException.class})
    OpenMeteoAirQualityResponse getCurrentAirQuality(
            @QueryParam("latitude") double latitude,
            @QueryParam("longitude") double longitude,
            @QueryParam("current") String current
    );
}
