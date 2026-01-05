package com.snappapp.snapng.enums;

public enum PaymentChannel {
    CARD("card"),
    BANK("bank"),
    USSD("ussd"),
    QR("qr"),
    MOBILE_MONEY("mobile_money"),
    BANK_TRANSFER("bank_transfer"),
    UNKNOWN("unknown"); // Fallback for unsupported channels

    private final String paystackValue;

    PaymentChannel(String paystackValue) {
        this.paystackValue = paystackValue;
    }

    public String getPaystackValue() {
        return paystackValue;
    }

    public static PaymentChannel fromPaystackValue(String paystackValue) {
        if (paystackValue == null) {
            return UNKNOWN;
        }
        for (PaymentChannel channel : values()) {
            if (channel.paystackValue.equalsIgnoreCase(paystackValue)) {
                return channel;
            }
        }
        return UNKNOWN;
    }
}