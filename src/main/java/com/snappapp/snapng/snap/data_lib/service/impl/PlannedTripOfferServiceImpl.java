package com.snappapp.snapng.snap.data_lib.service.impl;

import com.google.api.client.util.Strings;
import com.snappapp.snapng.exceptions.FailedProcessException;
import com.snappapp.snapng.exceptions.ResourceNotFoundException;
import com.snappapp.snapng.snap.data_lib.dtos.AddTripOfferDto;
import com.snappapp.snapng.snap.data_lib.entities.PlannedTrip;
import com.snappapp.snapng.snap.data_lib.entities.PlannedTripOffer;
import com.snappapp.snapng.snap.data_lib.entities.SnapUser;
import com.snappapp.snapng.snap.data_lib.enums.PlannedTripStatus;
import com.snappapp.snapng.snap.data_lib.enums.TripOfferStatus;
import com.snappapp.snapng.snap.data_lib.repositories.PlannedTripOfferRepository;
import com.snappapp.snapng.snap.data_lib.service.PlannedTripOfferService;
import com.snappapp.snapng.snap.utils.utilities.IdUtilities;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
public class PlannedTripOfferServiceImpl implements PlannedTripOfferService {
    private final PlannedTripOfferRepository repo;

    public PlannedTripOfferServiceImpl(PlannedTripOfferRepository repo) {
        this.repo = repo;
    }

    @Override
    public PlannedTripOffer save(AddTripOfferDto dto, PlannedTrip trip, SnapUser user) {
        PlannedTripOffer tripOffer = repo.findByTripAndUser(trip,user).orElse(new PlannedTripOffer());
        if(TripOfferStatus.ACCEPTED.equals(tripOffer.getStatus()) || !Strings.isNullOrEmpty(tripOffer.getBookingId())){
            throw new FailedProcessException("Your trip request has already been accepted");
        }
        tripOffer.setStatus(TripOfferStatus.PENDING);
        tripOffer.setDescription(dto.description());
        tripOffer.setTrip(trip);
        tripOffer.setReference(IdUtilities.useDateTimeAtomic());
        tripOffer.setUser(user);
        tripOffer.setWeight(dto.weight());
        tripOffer.setPickupLocation(dto.pickup());
        tripOffer.setDestinationLocation(dto.destination());
        tripOffer.setWorth(dto.worth());
        tripOffer.setUserProposedFee(dto.offer());
        tripOffer.setRecipientNumber(dto.phone());
        tripOffer.setBookingId(null);
        tripOffer.setBusinessProposedFee(null);
        tripOffer.setAdditionalNote(dto.note());
        return repo.save(tripOffer);
    }

    @Override
    public List<PlannedTripOffer> get(PlannedTrip trip) {
        return repo.findByTripAndStatusIn(trip, Arrays.asList(TripOfferStatus.ACCEPTED,
                TripOfferStatus.PENDING,TripOfferStatus.AWAITING_ACCEPTANCE));
    }

    @Override
    public List<PlannedTripOffer> getAccepted(PlannedTrip trip) {
        return repo.findByTripAndStatusIn(trip, Collections.singletonList(TripOfferStatus.ACCEPTED));
    }

    @Override
    public PlannedTripOffer get(SnapUser user, PlannedTrip trip) {
        if(PlannedTripStatus.CLOSED.equals(trip.getStatus())){
            throw new FailedProcessException("The trip is already closed");
        }
        return repo.findByTripAndUser(trip,user).orElseThrow(()->new ResourceNotFoundException("Trip offer not found"));
    }

    @Override
    public PlannedTripOffer setRiderOffer(String reference, Long offer) {
        PlannedTripOffer tripOffer = get(reference);
        if(PlannedTripStatus.CLOSED.equals(tripOffer.getTrip().getStatus())){
            throw new FailedProcessException("Trip is already closed");
        }
        if(TripOfferStatus.AWAITING_ACCEPTANCE.equals(tripOffer.getStatus()) || TripOfferStatus.PENDING.equals(tripOffer.getStatus())){
            tripOffer.setBusinessProposedFee(offer);
            tripOffer.setStatus(offer<=tripOffer.getUserProposedFee() ? TripOfferStatus.ACCEPTED : TripOfferStatus.AWAITING_ACCEPTANCE);
            return repo.save(tripOffer);
        }
        throw new FailedProcessException("A new offer can no longer be accepted");
    }

    @Override
    public PlannedTripOffer reject(String reference) {
        PlannedTripOffer tripOffer = get(reference);
        if(PlannedTripStatus.CLOSED.equals(tripOffer.getTrip().getStatus())){
            throw new FailedProcessException("Trip is already closed");
        }
        if(TripOfferStatus.ACCEPTED.equals(tripOffer.getStatus())){
            throw new FailedProcessException("Offer has already being accepted and cannot be canceled anymore");
        }
        tripOffer.setStatus(TripOfferStatus.CANCELED);
        return repo.save(tripOffer);
    }

    @Override
    public PlannedTripOffer get(String reference) {
        return repo.findByReference(reference).orElseThrow(()-> new ResourceNotFoundException("Trip offer not found"));
    }

    @Override
    public PlannedTripOffer accept(String reference,SnapUser user) {
        PlannedTripOffer tripOffer = repo.findByReferenceAndUser(reference,user).orElseThrow(() -> new ResourceNotFoundException("Trip offer not found"));
        if(PlannedTripStatus.CLOSED.equals(tripOffer.getTrip().getStatus())){
            throw new FailedProcessException("Trip is already closed");
        }
        if(TripOfferStatus.AWAITING_ACCEPTANCE.equals(tripOffer.getStatus())){
            tripOffer.setStatus(TripOfferStatus.ACCEPTED);
            return repo.save(tripOffer);
        }
        throw new FailedProcessException("Trip offer cannot be accepted at this time");
    }
}
