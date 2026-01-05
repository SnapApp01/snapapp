package com.snappapp.snapng.enums;

import lombok.Getter;

@Getter
public enum RoomType {
    SINGLE("Single Room"),
    DOUBLE("Double Room"),
    VILLA("Villa"),
    DELUXE("Deluxe Room"),
    FAMILY("Family Room"),
    PRESIDENTIAL("Presidential Suite");
    
    private final String displayName;
    
    RoomType(String displayName) {
        this.displayName = displayName;
    }

}