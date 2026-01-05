package com.snappapp.snapng.snap.data_lib.dtos;


import com.snappapp.snapng.snap.data_lib.entities.Location;
import com.snappapp.snapng.snap.data_lib.entities.Vehicle;

import java.time.LocalDate;

public record AddPlannedTripDto(Location start, Location end, Vehicle vehicle, LocalDate date) {
}
