package com.smartcomplaint.model;

/**
 * Complaint categories available in the system.
 */
public enum Category {
    HOSTEL("Hostel"),
    CLASSROOM("Classroom"),
    WIFI("Wi-Fi"),
    TRANSPORT("Transport"),
    ELECTRICITY("Electricity"),
    WATER("Water"),
    CLEANING("Cleaning"),
    MAINTENANCE("Maintenance"),
    OTHER("Other");

    private final String displayName;

    Category(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
