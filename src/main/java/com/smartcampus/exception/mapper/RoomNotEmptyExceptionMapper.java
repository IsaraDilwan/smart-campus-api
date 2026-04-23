package com.smartcampus.exception.mapper;

import com.smartcampus.exception.RoomNotEmptyException;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Maps RoomNotEmptyException to HTTP 409 Conflict.
 * Triggered when deleting a room that still has sensors.
 */
@Provider
public class RoomNotEmptyExceptionMapper implements ExceptionMapper<RoomNotEmptyException> {

    @Override
    public Response toResponse(RoomNotEmptyException exception) {
        Map<String, Object> errorResponse = new LinkedHashMap<>();
        errorResponse.put("error", "Resource Conflict");
        errorResponse.put("status", 409);
        errorResponse.put("message", exception.getMessage());
        errorResponse.put("roomId", exception.getRoomId());
        errorResponse.put("activeSensorCount", exception.getSensorCount());
        errorResponse.put("resolution", "Please remove or reassign all sensors from this room before attempting deletion.");

        return Response.status(Response.Status.CONFLICT)
                .entity(errorResponse)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
