package com.smartcampus.resource;

import com.smartcampus.data.DataStore;
import com.smartcampus.exception.SensorUnavailableException;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Sub-Resource class for SensorReadings.
 * Accessed via sub-resource locator in SensorResource.
 * Handles /api/v1/sensors/{sensorId}/readings
 */
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorReadingResource {

    private final String sensorId;

    public SensorReadingResource(String sensorId) {
        this.sensorId = sensorId;
    }

    /**
     * GET /api/v1/sensors/{sensorId}/readings
     * Returns full reading history for this sensor.
     */
    @GET
    public Response getAllReadings() {
        List<SensorReading> readings = DataStore.getReadings(sensorId);
        return Response.ok(readings).build();
    }

    /**
     * GET /api/v1/sensors/{sensorId}/readings/{readingId}
     * Returns a specific reading.
     */
    @GET
    @Path("/{readingId}")
    public Response getReadingById(@PathParam("readingId") String readingId) {
        List<SensorReading> readings = DataStore.getReadings(sensorId);
        for (SensorReading reading : readings) {
            if (reading.getId() != null && reading.getId().equals(readingId)) {
                return Response.ok(reading).build();
            }
        }
        return Response.status(Response.Status.NOT_FOUND)
                .entity(createErrorMap(404, "Not Found",
                        "Reading with ID '" + readingId + "' was not found for sensor '" + sensorId + "'."))
                .build();
    }

    /**
     * POST /api/v1/sensors/{sensorId}/readings
     * Appends new reading. Blocked if sensor is MAINTENANCE/OFFLINE (403).
     * Updates parent sensor's currentValue on success.
     */
    @POST
    public Response addReading(SensorReading reading) {
        Sensor parentSensor = DataStore.getSensor(sensorId);
        if (parentSensor == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(createErrorMap(404, "Not Found", "Parent sensor with ID '" + sensorId + "' was not found."))
                    .build();
        }

        String status = parentSensor.getStatus();
        if (status != null && (status.equalsIgnoreCase("MAINTENANCE") || status.equalsIgnoreCase("OFFLINE"))) {
            throw new SensorUnavailableException(sensorId, status);
        }

        if (reading.getId() == null || reading.getId().isEmpty()) {
            reading.setId(UUID.randomUUID().toString());
        }

        if (reading.getTimestamp() == 0) {
            reading.setTimestamp(System.currentTimeMillis());
        }

        DataStore.addReading(sensorId, reading);

        // Side effect: update parent sensor's currentValue
        parentSensor.setCurrentValue(reading.getValue());

        return Response.created(URI.create("/api/v1/sensors/" + sensorId + "/readings/" + reading.getId()))
                .entity(reading)
                .build();
    }

    private Map<String, Object> createErrorMap(int status, String error, String message) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("error", error);
        map.put("status", status);
        map.put("message", message);
        return map;
    }
}
