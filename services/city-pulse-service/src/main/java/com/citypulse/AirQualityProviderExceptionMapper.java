package com.citypulse;

import com.citypulse.airquality.AirQualityProviderException;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import org.jboss.logging.Logger;

@Provider
public class AirQualityProviderExceptionMapper implements ExceptionMapper<AirQualityProviderException> {

    private static final Logger LOG = Logger.getLogger(AirQualityProviderExceptionMapper.class);

    @Override
    public Response toResponse(AirQualityProviderException exception) {
        LOG.warn("Air quality provider call failed", exception);
        return Response.status(Response.Status.BAD_GATEWAY)
                .entity(new ApiError("AIR_QUALITY_PROVIDER_UNAVAILABLE", exception.getMessage()))
                .build();
    }
}
