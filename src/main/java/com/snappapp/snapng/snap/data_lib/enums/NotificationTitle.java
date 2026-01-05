package com.snappapp.snapng.snap.data_lib.enums;

import lombok.Getter;

@Getter
public enum NotificationTitle {
    TRANSACTION("Transaction Notification"),DELIVERY("Delivery Notification");

    final String title;
    NotificationTitle(String title){
        this.title = title;
    }

}
