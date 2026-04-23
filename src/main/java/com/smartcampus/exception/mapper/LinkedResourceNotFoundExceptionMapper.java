package com.smartcampus.exception.mapper;

import com.smartcampus.exception.LinkedResourceNotFoundException;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Maps LinkedResourceNotFoundException to HTTP 422 Unprocessable Entity.
 * Triggered when a payload references a non-existent linked resource.
 */
@Provider
public class LinkedResourceNotFoundExceptionMapper implements ExceptionMapper<LinkedResourceNotFoundException> {

    @Override
    public Response toResponse(LinkedResourceNotFoundException exception) {
        Map<String, Object> errorResponse = new LinkedHashMap<>();
        errorResponse.put("error", "Unprocessable Entity");
        errorResponse.put("status", 422);
        errorResponse.put("message", exception.getMessage());
        errorResponse.put("field", exception.getFieldName());
        errorResponse.put("rejectedValue", exception.getFieldValue());
        errorResponse.put("linkedResourceType", exception.getLinkedResourceType());

        return Response.status(422)
                .entity(errorResponse)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
