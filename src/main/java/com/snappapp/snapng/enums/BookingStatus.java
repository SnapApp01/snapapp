package com.snappapp.snapng.enums;

import lombok.Getter;

/**
 * Enumeration for booking status workflow.
 * 
 * @author HandyPros Team
 * @version 1.0
 * @since 1.0
 */
@Getter
public enum BookingStatus {
    PENDING("Pending"),
    CONFIRMED("Confirmed"),
    CANCELLED("Cancelled"),
    COMPLETED("Completed"),
    IN_PROGRESS("In Progress"),
    CHECKED_IN("Checked In"),
    CHECKED_OUT("Checked Out"),
    NO_SHOW("No Show"),
    EXPIRED("Expired");

    private final String displayName;

    BookingStatus(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }

    /**
     * Checks if the booking status allows cancellation.
     * 
     * @return true if cancellation is allowed
     */
    public boolean isCancellationAllowed() {
        return this == PENDING || this == CONFIRMED;
    }

    /**
     * Checks if the booking status allows modification.
     * 
     * @return true if modification is allowed
     */
    public boolean isModificationAllowed() {
        return this == PENDING || this == CONFIRMED;
    }

    /**
     * Checks if the booking is in an active state.
     * 
     * @return true if booking is active
     */
    public boolean isActive() {
        return this == CONFIRMED || this == IN_PROGRESS || this == CHECKED_IN;
    }

    /**
     * Checks if the booking is in a final state.
     * 
     * @return true if booking is in final state
     */
    public boolean isFinalState() {
        return this == COMPLETED || this == CANCELLED || this == NO_SHOW || this == EXPIRED;
    }
}