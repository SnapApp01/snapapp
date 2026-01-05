package com.snappapp.snapng.enums;

import lombok.Getter;

@Getter
public enum Currency {
    USD("US Dollar", "$"),
    EUR("Euro", "€"),
    NGN("Nigerian Naira", "₦");

    private final String displayName;
    private final String symbol;
    
    Currency(String displayName, String symbol) {
        this.displayName = displayName;
        this.symbol = symbol;
    }

}