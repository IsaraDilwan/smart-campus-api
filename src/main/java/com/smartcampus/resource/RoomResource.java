package com.smartcampus.resource;

import com.smartcampus.data.DataStore;
import com.smartcampus.exception.RoomNotEmptyException;
import com.smartcampus.model.Room;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Resource class for managing Room entities.
 * Handles CRUD operations on the /rooms path.
 */
@Path("/rooms")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RoomResource {

    /**
     * GET /api/v1/rooms
     * Returns a list of all rooms.
     */
    @GET
    public Response getAllRooms() {
        List<Room> rooms = new ArrayList<>(DataStore.getRooms().values());
        return Response.ok(rooms).build();
    }

    /**
     * GET /api/v1/rooms/{roomId}
     * Returns metadata for a specific room.
     */
    @GET
    @Path("/{roomId}")
    public Response getRoomById(@PathParam("roomId") String roomId) {
        Room room = DataStore.getRoom(roomId);
        if (room == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(createErrorMap(404, "Not Found", "Room with ID '" + roomId + "' was not found."))
                    .build();
        }
        return Response.ok(room).build();
    }

    /**
     * POST /api/v1/rooms
     * Creates a new room. Returns 201 Created with Location header.
     */
    @POST
    public Response createRoom(Room room) {
        if (room.getId() == null || room.getId().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(createErrorMap(400, "Bad Request", "Room 'id' is required and cannot be empty."))
                    .build();
        }

        if (DataStore.roomExists(room.getId())) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(createErrorMap(409, "Conflict", "A room with ID '" + room.getId() + "' already exists."))
                    .build();
        }

        if (room.getSensorIds() == null) {
            room.setSensorIds(new ArrayList<>());
        }

        DataStore.addRoom(room);

        return Response.created(URI.create("/api/v1/rooms/" + room.getId()))
                .entity(room)
                .build();
    }

    /**
     * DELETE /api/v1/rooms/{roomId}
     * Deletes a room. Blocked if room has active sensors (409 Conflict).
     * Idempotent: first call = 204, subsequent = 404, server state unchanged.
     */
    @DELETE
    @Path("/{roomId}")
    public Response deleteRoom(@PathParam("roomId") String roomId) {
        Room room = DataStore.getRoom(roomId);

        if (room == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(createErrorMap(404, "Not Found", "Room with ID '" + roomId + "' was not found."))
                    .build();
        }

        if (room.getSensorIds() != null && !room.getSensorIds().isEmpty()) {
            throw new RoomNotEmptyException(roomId, room.getSensorIds().size());
        }

        DataStore.removeRoom(roomId);
        return Response.noContent().build();
    }

    private Map<String, Object> createErrorMap(int status, String error, String message) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("error", error);
        map.put("status", status);
        map.put("message", message);
        return map;
    }
}
