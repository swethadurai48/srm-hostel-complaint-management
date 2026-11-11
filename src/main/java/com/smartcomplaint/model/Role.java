package com.smartcomplaint.model;

/**
 * Enterprise Roles supported in ERP & Smart Complaint Management System.
 */
public enum Role {
    SUPER_ADMIN("Super Administrator"),
    ADMIN("Administrator"),
    DEPT_MANAGER("Department Manager"),
    EMPLOYEE("Staff / Technician"),
    COMPLAINT_REPORTER("Complaint Reporter"),
    STUDENT("Student / Resident"); // Retained for backward compatibility

    private final String displayName;

    Role(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isSuperAdmin() {
        return this == SUPER_ADMIN;
    }

    public boolean isAdmin() {
        return this == ADMIN || this == SUPER_ADMIN;
    }

    public boolean isManager() {
        return this == DEPT_MANAGER || isAdmin();
    }

    public boolean isStaffOrEmployee() {
        return this == EMPLOYEE || this == DEPT_MANAGER || isAdmin();
    }

    public boolean isReporter() {
        return this == COMPLAINT_REPORTER || this == STUDENT;
    }
}
