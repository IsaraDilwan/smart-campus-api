package com.smartcampus.exception.mapper;

import com.smartcampus.exception.SensorUnavailableException;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Maps SensorUnavailableException to HTTP 403 Forbidden.
 * Triggered when posting a reading to a sensor in MAINTENANCE/OFFLINE status.
 */
@Provider
public class SensorUnavailableExceptionMapper implements ExceptionMapper<SensorUnavailableException> {

    @Override
    public Response toResponse(SensorUnavailableException exception) {
        Map<String, Object> errorResponse = new LinkedHashMap<>();
        errorResponse.put("error", "Forbidden");
        errorResponse.put("status", 403);
        errorResponse.put("message", exception.getMessage());
        errorResponse.put("sensorId", exception.getSensorId());
        errorResponse.put("currentStatus", exception.getCurrentStatus());
        errorResponse.put("resolution", "The sensor must be set to 'ACTIVE' status before it can accept readings.");

        return Response.status(Response.Status.FORBIDDEN)
                .entity(errorResponse)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
