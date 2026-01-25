package com.snappapp.snapng.snap.app_service.controllers;

import com.snappapp.snapng.enums.WalletType;
import com.snappapp.snapng.snap.app_service.apimodels.FundWalletRequest;
import com.snappapp.snapng.snap.app_service.apimodels.FundWalletResponse;
import com.snappapp.snapng.snap.app_service.apimodels.WalletTransactionResponse;
import com.snappapp.snapng.snap.app_service.apimodels.WithdrawalRequest;
import com.snappapp.snapng.snap.app_service.services.WalletManagementService;
import com.snappapp.snapng.snap.data_lib.entities.SnapUser;
import com.snappapp.snapng.utills.SecurityUtil;
import org.springframework.data.domain.Page;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/wallet")
public class WalletController {
    private final WalletManagementService service;
    private final SecurityUtil securityUtil;

    public WalletController(WalletManagementService service, SecurityUtil securityUtil) {
        this.service = service;
        this.securityUtil = securityUtil;
    }

    @PostMapping("/fund")
    public FundWalletResponse fund(@Validated @RequestBody FundWalletRequest request){
        SnapUser user = securityUtil.getCurrentLoggedInUser();
        return service.initiateWalletFunding(request, user.getId());
    }

    @GetMapping("/transactions")
    public Page<WalletTransactionResponse> getTransactions(
            @RequestParam(defaultValue = "SNAP_USER") WalletType walletType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size
    ) {
        SnapUser user = securityUtil.getCurrentLoggedInUser();
        return service.getTransactions(user.getId(), walletType, page, size);
    }

//    @GetMapping("/transactions")
//    public List<WalletTransactionResponse> getTransactions(){
//        SnapUser user = securityUtil.getCurrentLoggedInUser();
//        return service.getTransactions(user.getId(), 0,50).getContent();
//    }

    @PostMapping("/withdraw")
    public void withdraw(@Validated @RequestBody WithdrawalRequest request){
        SnapUser user = securityUtil.getCurrentLoggedInUser();
        service.initiateWithdrawal(user.getId(), request);
    }
}
