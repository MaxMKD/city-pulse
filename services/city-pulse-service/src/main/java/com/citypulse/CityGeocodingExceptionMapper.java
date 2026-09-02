package com.citypulse;

import com.citypulse.city.CityGeocodingException;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import org.jboss.logging.Logger;

@Provider
public class CityGeocodingExceptionMapper implements ExceptionMapper<CityGeocodingException> {

    private static final Logger LOG = Logger.getLogger(CityGeocodingExceptionMapper.class);

    @Override
    public Response toResponse(CityGeocodingException exception) {
        LOG.warn("City geocoding provider call failed", exception);
        return Response.status(Response.Status.BAD_GATEWAY)
                .entity(new ApiError("CITY_SEARCH_PROVIDER_UNAVAILABLE", exception.getMessage()))
                .build();
    }
}
