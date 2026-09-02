package com.citypulse;

import com.citypulse.weather.WeatherProviderException;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import org.jboss.logging.Logger;

@Provider
public class WeatherProviderExceptionMapper implements ExceptionMapper<WeatherProviderException> {

    private static final Logger LOG = Logger.getLogger(WeatherProviderExceptionMapper.class);

    @Override
    public Response toResponse(WeatherProviderException exception) {
        LOG.warn("Weather provider call failed", exception);
        return Response.status(Response.Status.BAD_GATEWAY)
                .entity(new ApiError("WEATHER_PROVIDER_UNAVAILABLE", exception.getMessage()))
                .build();
    }
}
