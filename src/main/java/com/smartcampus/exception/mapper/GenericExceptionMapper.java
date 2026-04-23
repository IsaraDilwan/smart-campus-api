package com.smartcampus.exception.mapper;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Global "safety net" ExceptionMapper that catches any unhandled exceptions.
 * Returns a clean 500 status — never leaks Java stack traces to the client.
 * Full stack trace is logged server-side for debugging.
 */
@Provider
public class GenericExceptionMapper implements ExceptionMapper<Throwable> {

    private static final Logger LOGGER = Logger.getLogger(GenericExceptionMapper.class.getName());

    @Override
    public Response toResponse(Throwable exception) {
        // Log the full stack trace server-side for debugging
        LOGGER.log(Level.SEVERE, "Unhandled exception caught by global safety net", exception);

        Map<String, Object> errorResponse = new LinkedHashMap<>();
        errorResponse.put("error", "Internal Server Error");
        errorResponse.put("status", 500);
        errorResponse.put("message", "An unexpected error occurred on the server. Please contact the system administrator.");

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(errorResponse)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
