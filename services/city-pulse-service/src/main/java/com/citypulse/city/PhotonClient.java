package com.citypulse.city;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.ServerErrorException;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

// configKey "photon" ties this to the quarkus.rest-client.photon.* properties
@RegisterRestClient(configKey = "photon")
@Path("/api")
@Produces(MediaType.APPLICATION_JSON)
public interface PhotonClient {

    // Retries only transient failures (network issues, 5xx); 4xx client errors are never retried.
    // lang is pinned to "en" so country names are consistent regardless of caller/server locale
    // (Photon otherwise returns names like "Schweiz/Suisse/Svizzera/Svizra" for Switzerland).
    @GET
    @Retry(maxRetries = 2, delay = 200, jitter = 100, retryOn = {ProcessingException.class, ServerErrorException.class})
    PhotonResponse search(
            @QueryParam("q") String query,
            @QueryParam("limit") int limit,
            @QueryParam("lang") String language
    );
}
