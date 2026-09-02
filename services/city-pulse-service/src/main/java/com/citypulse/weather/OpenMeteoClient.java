package com.citypulse.weather;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.ServerErrorException;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

// configKey "open-meteo" ties this to the quarkus.rest-client.open-meteo.* properties
@RegisterRestClient(configKey = "open-meteo")
@Path("/v1/forecast")
@Produces(MediaType.APPLICATION_JSON)
public interface OpenMeteoClient {

    // Retries only transient failures (network issues, 5xx); 4xx client errors are never retried.
    @GET
    @Retry(maxRetries = 2, delay = 200, jitter = 100, retryOn = {ProcessingException.class, ServerErrorException.class})
    OpenMeteoResponse getCurrentWeather(
            @QueryParam("latitude") double latitude,
            @QueryParam("longitude") double longitude,
            @QueryParam("current") String current
    );
}
