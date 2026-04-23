package com.smartcampus.resource;

import com.smartcampus.data.DataStore;
import com.smartcampus.exception.LinkedResourceNotFoundException;
import com.smartcampus.model.Room;
import com.smartcampus.model.Sensor;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Resource class for managing Sensor entities.
 * Handles CRUD, filtering, and sub-resource locator for readings.
 */
@Path("/sensors")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorResource {

    /**
     * GET /api/v1/sensors
     * Returns all sensors. Supports optional filtering by type.
     * Example: GET /api/v1/sensors?type=CO2
     */
    @GET
    public Response getAllSensors(@QueryParam("type") String type) {
        List<Sensor> sensors = new ArrayList<>(DataStore.getSensors().values());

        if (type != null && !type.isEmpty()) {
            sensors = sensors.stream()
                    .filter(s -> s.getType() != null && s.getType().equalsIgnoreCase(type))
                    .collect(Collectors.toList());
        }

        return Response.ok(sensors).build();
    }

    /**
     * GET /api/v1/sensors/{sensorId}
     * Returns info for a specific sensor.
     */
    @GET
    @Path("/{sensorId}")
    public Response getSensorById(@PathParam("sensorId") String sensorId) {
        Sensor sensor = DataStore.getSensor(sensorId);
        if (sensor == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(createErrorMap(404, "Not Found", "Sensor with ID '" + sensorId + "' was not found."))
                    .build();
        }
        return Response.ok(sensor).build();
    }

    /**
     * POST /api/v1/sensors
     * Registers a new sensor. roomId MUST reference an existing room.
     */
    @POST
    public Response createSensor(Sensor sensor) {
        if (sensor.getId() == null || sensor.getId().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(createErrorMap(400, "Bad Request", "Sensor 'id' is required and cannot be empty."))
                    .build();
        }

        if (DataStore.sensorExists(sensor.getId())) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(createErrorMap(409, "Conflict", "A sensor with ID '" + sensor.getId() + "' already exists."))
                    .build();
        }

        if (sensor.getRoomId() == null || sensor.getRoomId().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(createErrorMap(400, "Bad Request", "Sensor 'roomId' is required and cannot be empty."))
                    .build();
        }

        if (!DataStore.roomExists(sensor.getRoomId())) {
            throw new LinkedResourceNotFoundException("roomId", sensor.getRoomId(), "Room");
        }

        if (sensor.getStatus() == null || sensor.getStatus().isEmpty()) {
            sensor.setStatus("ACTIVE");
        }

        DataStore.addSensor(sensor);

        Room parentRoom = DataStore.getRoom(sensor.getRoomId());
        if (parentRoom != null) {
            parentRoom.addSensorId(sensor.getId());
        }

        return Response.created(URI.create("/api/v1/sensors/" + sensor.getId()))
                .entity(sensor)
                .build();
    }

    /**
     * DELETE /api/v1/sensors/{sensorId}
     * Removes a sensor and unlinks it from its parent room.
     */
    @DELETE
    @Path("/{sensorId}")
    public Response deleteSensor(@PathParam("sensorId") String sensorId) {
        Sensor sensor = DataStore.getSensor(sensorId);
        if (sensor == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(createErrorMap(404, "Not Found", "Sensor with ID '" + sensorId + "' was not found."))
                    .build();
        }

        Room parentRoom = DataStore.getRoom(sensor.getRoomId());
        if (parentRoom != null) {
            parentRoom.removeSensorId(sensorId);
        }

        DataStore.removeSensor(sensorId);
        return Response.noContent().build();
    }

    /**
     * Sub-Resource Locator for Sensor Readings.
     * Delegates /sensors/{sensorId}/readings to SensorReadingResource.
     */
    @Path("/{sensorId}/readings")
    public SensorReadingResource getSensorReadingResource(@PathParam("sensorId") String sensorId) {
        Sensor sensor = DataStore.getSensor(sensorId);
        if (sensor == null) {
            throw new NotFoundException("Sensor with ID '" + sensorId + "' was not found.");
        }
        return new SensorReadingResource(sensorId);
    }

    private Map<String, Object> createErrorMap(int status, String error, String message) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("error", error);
        map.put("status", status);
        map.put("message", message);
        return map;
    }
}
