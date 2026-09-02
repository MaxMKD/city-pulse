package com.citypulse;

import com.citypulse.city.CityNotFoundException;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class CityNotFoundExceptionMapper implements ExceptionMapper<CityNotFoundException> {

    @Override
    public Response toResponse(CityNotFoundException exception) {
        return Response.status(Response.Status.NOT_FOUND)
                .entity(new ApiError("CITY_NOT_FOUND", exception.getMessage()))
                .build();
    }
}
