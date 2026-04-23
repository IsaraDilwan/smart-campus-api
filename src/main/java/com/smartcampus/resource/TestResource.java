package com.smartcampus.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Test Resource — used to demonstrate the Global Safety Net (500 ExceptionMapper).
 * 
 * This endpoint deliberately triggers an unhandled RuntimeException to prove
 * that the GenericExceptionMapper catches it and returns a clean JSON response
 * instead of exposing a raw Java stack trace.
 */
@Path("/test")
@Produces(MediaType.APPLICATION_JSON)
public class TestResource {

    /**
     * GET /api/v1/test/error
     * Deliberately throws a NullPointerException to demonstrate the
     * catch-all ExceptionMapper returning a clean 500 response.
     */
    @GET
    @Path("/error")
    public Response triggerError() {
        // Deliberately cause an unhandled exception
        String nullString = null;
        nullString.length(); // This will throw NullPointerException
        return Response.ok().build(); // Never reached
    }
}
