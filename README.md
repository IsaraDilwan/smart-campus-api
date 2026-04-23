# Smart Campus Sensor & Room Management API

## Overview

The **Smart Campus API** is a RESTful web service built with **JAX-RS (Jersey 2.x)** deployed on **Apache Tomcat**. It manages Rooms, Sensors, and Sensor Readings for a university's Smart Campus initiative.

**Key features:**
- Full CRUD operations for Rooms and Sensors
- Sub-resource pattern for Sensor Readings (historical data)
- Comprehensive error handling with custom ExceptionMappers (409, 422, 403, 500)
- Request/Response logging filter for API observability
- HATEOAS-style discovery endpoint
- In-memory data storage using thread-safe ConcurrentHashMap

---

## How to Build and Run (NetBeans + Tomcat)

### Prerequisites
- **Java JDK 11** or higher
- **Apache NetBeans** (with Tomcat bundled)
- **Apache Tomcat 9.x or 10.x** (usually bundled with NetBeans)

### Step-by-Step

1. **Clone the repository:**
   ```bash
   git clone https://github.com/YOUR_USERNAME/smart-campus-api.git
   ```

2. **Open in NetBeans:**
   - File → Open Project → select the `smart-campus-v3` folder
   - Wait for Maven to download all dependencies

3. **Add Tomcat Server (if not already configured):**
   - Tools → Servers → Add Server → Apache Tomcat
   - Point it to your Tomcat installation directory

4. **Run the project:**
   - Right-click the project → Run
   - NetBeans will build the WAR, deploy to Tomcat, and open the browser
   - The API is available at: `http://localhost:8080/smart-campus-api/api/v1`

5. **Stop the server:**
   - Right-click the project → Stop or use the red Stop button

---

## API Endpoints

| Method | URL | Description |
|--------|-----|-------------|
| GET | /api/v1 | Discovery endpoint (metadata + HATEOAS links) |
| GET | /api/v1/rooms | List all rooms |
| POST | /api/v1/rooms | Create a new room |
| GET | /api/v1/rooms/{id} | Get a specific room |
| DELETE | /api/v1/rooms/{id} | Delete a room (blocked if has sensors) |
| GET | /api/v1/sensors | List all sensors (optional: ?type=CO2) |
| POST | /api/v1/sensors | Register a new sensor |
| GET | /api/v1/sensors/{id} | Get a specific sensor |
| DELETE | /api/v1/sensors/{id} | Delete a sensor |
| GET | /api/v1/sensors/{id}/readings | Get reading history |
| POST | /api/v1/sensors/{id}/readings | Add a new reading |
| GET | /api/v1/test/error | Trigger 500 error (demonstrates safety net) |

**Base URL:** `http://localhost:8080/smart-campus-api/api/v1`

---

## Sample curl Commands

```bash
# 1. Discovery endpoint
curl -X GET http://localhost:8080/smart-campus-api/api/v1

# 2. Create a room
curl -X POST http://localhost:8080/smart-campus-api/api/v1/rooms \
  -H "Content-Type: application/json" \
  -d '{"id":"LIB-301","name":"Library Quiet Study","capacity":50}'

# 3. Get all rooms
curl -X GET http://localhost:8080/smart-campus-api/api/v1/rooms

# 4. Create a sensor
curl -X POST http://localhost:8080/smart-campus-api/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{"id":"TEMP-001","type":"Temperature","status":"ACTIVE","currentValue":22.5,"roomId":"LIB-301"}'

# 5. Filter sensors by type
curl -X GET "http://localhost:8080/smart-campus-api/api/v1/sensors?type=Temperature"

# 6. Post a sensor reading
curl -X POST http://localhost:8080/smart-campus-api/api/v1/sensors/TEMP-001/readings \
  -H "Content-Type: application/json" \
  -d '{"value":23.7}'

# 7. Get reading history
curl -X GET http://localhost:8080/smart-campus-api/api/v1/sensors/TEMP-001/readings

# 8. Try deleting room with sensors (409 Conflict)
curl -X DELETE http://localhost:8080/smart-campus-api/api/v1/rooms/LIB-301

# 9. Create sensor with fake roomId (422 error)
curl -X POST http://localhost:8080/smart-campus-api/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{"id":"CO2-005","type":"CO2","status":"ACTIVE","currentValue":400,"roomId":"FAKE-ROOM"}'

# 10. Trigger 500 error (safety net test)
curl -X GET http://localhost:8080/smart-campus-api/api/v1/test/error
```

---

## Report: Answers to Coursework Questions

### Part 1: Service Architecture & Setup

**Q1.1: Explain the default lifecycle of a JAX-RS Resource class. Is a new instance created for every request, or is it a singleton? How does this affect in-memory data?**

By default, JAX-RS resource classes are **request-scoped**. The runtime creates a new instance for every incoming HTTP request. After the response is sent, that instance is discarded.

This means if we stored data in an instance variable (like a HashMap inside the resource class), that data would vanish after each request. To solve this, we use a separate static `DataStore` class with `ConcurrentHashMap` fields. Static fields belong to the class itself, so all request-scoped instances share the same data. We use `ConcurrentHashMap` instead of `HashMap` because multiple request threads may access the data simultaneously. Without thread-safe collections, concurrent access could cause race conditions or `ConcurrentModificationException` errors. `ConcurrentHashMap` provides fine-grained locking for safe concurrent reads and writes.

**Q1.2: Why is HATEOAS considered a hallmark of advanced RESTful design?**

HATEOAS makes the API self-describing and navigable. Instead of clients needing to hard-code URLs, the API provides links to related resources in its responses. Benefits over static documentation include: (1) Decoupling — if URLs change, clients discover new paths dynamically. (2) Discoverability — clients can navigate the entire API from the root endpoint. (3) Accuracy — HATEOAS responses are always current, unlike static docs that can become stale. (4) Simpler client development — clients adapt to changes automatically. Our discovery endpoint at GET /api/v1 returns a map of all primary resource URIs.

### Part 2: Room Management

**Q2.1: Implications of returning only IDs versus full room objects?**

Returning only IDs minimises payload size but forces the client to make additional HTTP requests per room (the N+1 problem), which is far more expensive in latency. Returning full objects provides everything in one request. The payload is larger but eliminates follow-up requests. For a campus scenario with a moderate number of rooms, full objects are preferred because network latency is the main bottleneck. For very large datasets, pagination would be ideal.

**Q2.2: Is DELETE idempotent in your implementation?**

Yes. First call: room exists and is deleted, returns 204. Second call: room is gone, returns 404. Server state is unchanged. All further calls: same 404, no state change. The different response codes do not violate idempotency because idempotency is about the server-side effect, not the response code.

### Part 3: Sensor Operations & Linking

**Q3.1: What happens if a client sends text/plain or application/xml to a method with @Consumes(APPLICATION_JSON)?**

JAX-RS automatically rejects the request with HTTP 415 Unsupported Media Type before the method code runs. The framework checks the Content-Type header against the @Consumes annotation and short-circuits if there is no match. No manual validation needed.

**Q3.2: Why is @QueryParam superior to path-based filtering?**

(1) Semantic correctness — path segments identify resources, not filters. /sensors/type/CO2 implies "type" is a sub-resource, which it is not. (2) Optional parameters — query params are inherently optional; the same endpoint serves filtered and unfiltered requests. (3) Composability — multiple filters combine naturally: ?type=CO2&status=ACTIVE. Path-based filtering creates unwieldy nested URLs. (4) Convention — major APIs (Google, GitHub) all use query parameters for filtering.

### Part 4: Deep Nesting with Sub-Resources

**Q4.1: Benefits of the Sub-Resource Locator pattern?**

(1) Separation of concerns — each class handles one entity. SensorResource handles sensors, SensorReadingResource handles readings. (2) Code organisation — nesting levels map to separate classes. (3) Reusability — sub-resource classes can be reused from different parents. (4) Testability — smaller classes are easier to test independently. (5) Boundary validation — the locator method validates the parent exists before delegation.

### Part 5: Error Handling & Logging

**Q5.1: Why is 422 more accurate than 404 for missing references in a payload?**

404 means the requested URL does not exist. But when POSTing to /sensors with a bad roomId, the /sensors URL works fine — the problem is in the data. 422 correctly says: the JSON is valid, but it contains a semantic error (an invalid reference). 404 would mislead developers into debugging the URL instead of the payload.

**Q5.2: Cybersecurity risks of exposing stack traces?**

Stack traces reveal: (1) Internal class names and package structure. (2) Library versions — attackers search for CVEs in those versions. (3) File paths and line numbers — reveals OS and deployment info. (4) Application logic — the call chain exposes business logic and injection points. (5) Database details — table names, queries. Our GenericExceptionMapper returns a clean 500 JSON response and logs the trace server-side only.

**Q5.3: Why use filters instead of manual Logger.info() in every method?**

(1) Automatic coverage — one filter covers all endpoints including future ones. (2) Consistency — uniform log format everywhere. (3) Clean business logic — methods stay focused. (4) Single point of change — modify logging in one place. (5) Framework integration — filters access request/response contexts directly.

---

## Project Structure

```
smart-campus-v3/
├── pom.xml
├── README.md
└── src/main/
    ├── java/com/smartcampus/
    │   ├── SmartCampusApplication.java        # @ApplicationPath("/api/v1")
    │   ├── data/
    │   │   └── DataStore.java                 # In-memory store (ConcurrentHashMap)
    │   ├── model/
    │   │   ├── Room.java
    │   │   ├── Sensor.java
    │   │   └── SensorReading.java
    │   ├── resource/
    │   │   ├── DiscoveryResource.java         # GET / (metadata + HATEOAS)
    │   │   ├── RoomResource.java              # /rooms CRUD
    │   │   ├── SensorResource.java            # /sensors CRUD + sub-resource locator
    │   │   ├── SensorReadingResource.java     # Sub-resource for readings
    │   │   └── TestResource.java              # /test/error (500 safety net demo)
    │   ├── exception/
    │   │   ├── RoomNotEmptyException.java             # → 409
    │   │   ├── LinkedResourceNotFoundException.java   # → 422
    │   │   ├── SensorUnavailableException.java        # → 403
    │   │   └── mapper/
    │   │       ├── RoomNotEmptyExceptionMapper.java
    │   │       ├── LinkedResourceNotFoundExceptionMapper.java
    │   │       ├── SensorUnavailableExceptionMapper.java
    │   │       └── GenericExceptionMapper.java        # → 500 catch-all
    │   └── filter/
    │       └── LoggingFilter.java             # Request/Response logging
    └── webapp/WEB-INF/
        └── web.xml                            # Jersey servlet config for Tomcat
```

## Technology Stack

| Component | Technology |
|-----------|-----------|
| Framework | JAX-RS (Jersey 2.41) |
| Server | Apache Tomcat |
| JSON | Jackson |
| Build Tool | Apache Maven |
| Data Storage | In-memory (ConcurrentHashMap) |
| Language | Java 11 |
