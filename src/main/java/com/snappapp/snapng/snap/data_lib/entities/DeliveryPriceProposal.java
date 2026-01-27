package com.snappapp.snapng.snap.data_lib.entities;

import com.snappapp.snapng.models.baseclass.BaseEntity;
import com.snappapp.snapng.snap.data_lib.enums.FeeProposalStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "delivery_price_proposals")
@Getter
@Setter
public class DeliveryPriceProposal extends BaseEntity {
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "vehicle_id",referencedColumnName = "id")
    private Vehicle vehicle;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "request_id",referencedColumnName = "id")
    private DeliveryRequest request;
    @Column(unique = true)
    private String proposalId;
    private String comment;
    private Long fee;
    @Enumerated(EnumType.STRING)
    private FeeProposalStatus status;
    private Boolean businessInitiated;
    private String businessUserId;
    private Long counterProposal;
}
