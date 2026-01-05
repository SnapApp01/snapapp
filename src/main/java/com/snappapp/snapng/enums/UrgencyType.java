package com.snappapp.snapng.enums;

import lombok.Getter;

@Getter
public enum UrgencyType {
    BEST_SELLER("Best Seller"),
    NEW("New"),
    LIMITED_OFFER("Limited Offer"),
    ALMOST_SOLD_OUT("Almost Sold Out"),
    LAST_CHANCE("Last Chance"),
    FLASH_SALE("Flash Sale");
    
    private final String displayName;
    
    UrgencyType(String displayName) {
        this.displayName = displayName;
    }

}