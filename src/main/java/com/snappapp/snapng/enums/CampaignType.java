package com.snappapp.snapng.enums;

import lombok.Getter;

@Getter
public enum CampaignType {
    AFCON_2025("AFCON 2025"),
    DIRTY_DECEMBER("Dirty December"),
    EASTER_DEALS("Easter Deals"),
    SUMMER_SPECIAL("Summer Special"),
    BLACK_FRIDAY("Black Friday"),
    NEW_YEAR("New Year Special");
    
    private final String displayName;
    
    CampaignType(String displayName) {
        this.displayName = displayName;
    }

}