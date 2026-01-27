package com.snappapp.snapng.snap.app_service.apimodels;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdatePriceProposalRequest {
    private String proposalId;
    private boolean accept;
    private Long counterProposal;
}
