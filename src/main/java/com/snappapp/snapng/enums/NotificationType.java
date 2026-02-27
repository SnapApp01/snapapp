package com.snappapp.snapng.enums;

/**
 * Enum to distinguish notification recipients based on their role
 * 
 * USER - Notifications for users acting as customers (delivery senders)
 * DRIVER - Notifications for users acting as business owners/drivers
 */
public enum NotificationType {
    USER("User/Customer notifications"),
    DRIVER("Driver/Business owner notifications");
    
    private final String description;
    
    NotificationType(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}