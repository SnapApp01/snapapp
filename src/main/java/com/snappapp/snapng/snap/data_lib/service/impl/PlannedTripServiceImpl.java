package com.snappapp.snapng.snap.data_lib.service.impl;

import com.google.api.client.util.Strings;
import com.snappapp.snapng.exceptions.ResourceNotFoundException;
import com.snappapp.snapng.snap.data_lib.dtos.AddPlannedTripDto;
import com.snappapp.snapng.snap.data_lib.entities.Business;
import com.snappapp.snapng.snap.data_lib.entities.PlannedTrip;
import com.snappapp.snapng.snap.data_lib.enums.PlannedTripStatus;
import com.snappapp.snapng.snap.data_lib.repositories.PlannedTripRepository;
import com.snappapp.snapng.snap.data_lib.service.PlannedTripService;
import com.snappapp.snapng.snap.utils.utilities.IdUtilities;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@Slf4j
public class PlannedTripServiceImpl implements PlannedTripService {

    private final PlannedTripRepository repo;

    public PlannedTripServiceImpl(PlannedTripRepository repo) {
        this.repo = repo;
    }

    @Override
    public PlannedTrip getPlannedTrip(String reference) {
        return repo.findByReference(reference).orElseThrow(()->new ResourceNotFoundException("Planned Trip with reference not found"));
    }

    @Override
    public List<PlannedTrip> getPlannedTrips() {
        return repo.findByStatus(PlannedTripStatus.OPEN, PageRequest.of(0,25, Sort.Direction.ASC,"tripDate")).getContent();
    }

    @Override
    public List<PlannedTrip> getPlannedTrips(Business business) {
        return repo.findByBusiness(business);
    }

    @Override
    public PlannedTrip save(AddPlannedTripDto dto, Business business) {
        PlannedTrip plannedTrip = new PlannedTrip();
        plannedTrip.setTripDate(dto.date());
        plannedTrip.setEnd(dto.end());
        plannedTrip.setStart(dto.start());
        plannedTrip.setStatus(PlannedTripStatus.OPEN);
        plannedTrip.setBusiness(business);
        plannedTrip.setVehicle(dto.vehicle());
        plannedTrip.setReference(IdUtilities.useUUID());
        return repo.save(plannedTrip);
    }

    @Override
    public PlannedTrip update(PlannedTripStatus status, String reference, String businessId) {
        PlannedTrip trip = getPlannedTrip(reference);
        if(Strings.isNullOrEmpty(businessId) || !businessId.equalsIgnoreCase(trip.getBusiness().getCode())){
            throw new ResourceNotFoundException("Planned trip with reference not found");
        }
        if(LocalDate.now().isAfter(trip.getTripDate())){
            trip.setStatus(PlannedTripStatus.CLOSED);
        }
        else{
            trip.setStatus(status);
        }
        return repo.save(trip);
    }
}
