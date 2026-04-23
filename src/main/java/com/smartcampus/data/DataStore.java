package com.smartcampus.data;

import com.smartcampus.model.Room;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Centralized in-memory data store for the Smart Campus API.
 * 
 * Uses ConcurrentHashMap to ensure thread-safety since JAX-RS resource classes
 * are request-scoped by default (a new instance per request). Multiple threads
 * may access the shared data concurrently, so we need thread-safe collections.
 */
public class DataStore {

    // Thread-safe maps for storing entities
    private static final Map<String, Room> rooms = new ConcurrentHashMap<>();
    private static final Map<String, Sensor> sensors = new ConcurrentHashMap<>();
    private static final Map<String, List<SensorReading>> sensorReadings = new ConcurrentHashMap<>();

    private DataStore() {
    }

    // ==================== Room Operations ====================

    public static Map<String, Room> getRooms() {
        return rooms;
    }

    public static Room getRoom(String id) {
        return rooms.get(id);
    }

    public static void addRoom(Room room) {
        rooms.put(room.getId(), room);
    }

    public static Room removeRoom(String id) {
        return rooms.remove(id);
    }

    public static boolean roomExists(String id) {
        return rooms.containsKey(id);
    }

    // ==================== Sensor Operations ====================

    public static Map<String, Sensor> getSensors() {
        return sensors;
    }

    public static Sensor getSensor(String id) {
        return sensors.get(id);
    }

    public static void addSensor(Sensor sensor) {
        sensors.put(sensor.getId(), sensor);
    }

    public static Sensor removeSensor(String id) {
        return sensors.remove(id);
    }

    public static boolean sensorExists(String id) {
        return sensors.containsKey(id);
    }

    // ==================== Sensor Reading Operations ====================

    public static List<SensorReading> getReadings(String sensorId) {
        return sensorReadings.getOrDefault(sensorId, new ArrayList<>());
    }

    public static void addReading(String sensorId, SensorReading reading) {
        sensorReadings.computeIfAbsent(sensorId, k -> new ArrayList<>()).add(reading);
    }
}
