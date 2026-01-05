package com.snappapp.snapng.snap.data_lib.dtos;


import com.snappapp.snapng.snap.data_lib.entities.Location;

public record AddTripOfferDto(String description,
                              Long worth, String weight,
                              Location pickup, Location destination,
                              String name, String phone, Long offer, String note) {
}
