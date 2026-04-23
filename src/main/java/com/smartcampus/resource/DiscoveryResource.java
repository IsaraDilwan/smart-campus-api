package com.smartcampus.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Discovery Resource — the root entry point of the Smart Campus API.
 * Returns API metadata including versioning, contact, and resource URIs (HATEOAS).
 */
@Path("/")
@Produces(MediaType.APPLICATION_JSON)
public class DiscoveryResource {

    @GET
    public Response getApiMetadata() {
        Map<String, Object> metadata = new LinkedHashMap<>();

        metadata.put("apiName", "Smart Campus Sensor & Room Management API");
        metadata.put("version", "1.0");
        metadata.put("description", "A RESTful API for managing rooms and sensors across the university's Smart Campus initiative.");

        Map<String, String> contact = new LinkedHashMap<>();
        contact.put("name", "Smart Campus Admin Team");
        contact.put("email", "admin@smartcampus.university.ac.uk");
        contact.put("department", "Facilities Management");
        metadata.put("contact", contact);

        Map<String, String> resources = new LinkedHashMap<>();
        resources.put("rooms", "/api/v1/rooms");
        resources.put("sensors", "/api/v1/sensors");
        metadata.put("resources", resources);

        metadata.put("serverTime", System.currentTimeMillis());

        return Response.ok(metadata).build();
    }
}
