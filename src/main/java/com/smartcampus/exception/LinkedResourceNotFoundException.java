package com.smartcampus.exception;

/**
 * Thrown when a resource references a non-existent linked resource
 * (e.g., a Sensor with an invalid roomId).
 * Mapped to HTTP 422 Unprocessable Entity.
 */
public class LinkedResourceNotFoundException extends RuntimeException {

    private final String fieldName;
    private final String fieldValue;
    private final String linkedResourceType;

    public LinkedResourceNotFoundException(String fieldName, String fieldValue, String linkedResourceType) {
        super("The referenced " + linkedResourceType + " with " + fieldName
              + " '" + fieldValue + "' does not exist in the system.");
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
        this.linkedResourceType = linkedResourceType;
    }

    public String getFieldName() {
        return fieldName;
    }

    public String getFieldValue() {
        return fieldValue;
    }

    public String getLinkedResourceType() {
        return linkedResourceType;
    }
}
