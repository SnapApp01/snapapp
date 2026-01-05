package com.snappapp.snapng.snap.app_service.controllers;

import com.snappapp.snapng.snap.app_service.apimodels.AddBankAccountRequest;
import com.snappapp.snapng.snap.app_service.apimodels.Bank;
import com.snappapp.snapng.snap.app_service.apimodels.BankAccountResponse;
import com.snappapp.snapng.snap.app_service.apimodels.NameEnquiryResponse;
import com.snappapp.snapng.snap.data_lib.dtos.BankAccountDto;
import com.snappapp.snapng.snap.data_lib.entities.BankAccount;
import com.snappapp.snapng.snap.data_lib.entities.Business;
import com.snappapp.snapng.snap.data_lib.entities.SnapUser;
import com.snappapp.snapng.snap.data_lib.service.BankAccountService;
import com.snappapp.snapng.snap.data_lib.service.BusinessService;
import com.snappapp.snapng.snap.data_lib.service.SnapUserService;
import com.snappapp.snapng.snap.payment_util.paystack.AccountEnquiryResponse;
import com.snappapp.snapng.snap.payment_util.paystack.BankResponse;
import com.snappapp.snapng.snap.payment_util.services.PaystackService;
import com.snappapp.snapng.utills.SecurityUtil;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/api/v1/bank")
@RestController
public class BankAccountController {

    private final PaystackService paystackService;
    private final BusinessService businessService;
    private final SnapUserService userService;
    private final BankAccountService bankAccountService;
    private final SecurityUtil securityUtil;

    public BankAccountController(PaystackService paystackService, BusinessService businessService, SnapUserService userService, BankAccountService bankAccountService, SecurityUtil securityUtil) {
        this.paystackService = paystackService;
        this.businessService = businessService;
        this.userService = userService;
        this.bankAccountService = bankAccountService;
        this.securityUtil = securityUtil;
    }


    @GetMapping("/bank-list")
    public List<Bank> getBanks(){
        List<BankResponse> banks = paystackService.getBanks();
        return banks.stream().map(e->Bank.builder()
                .code(e.getCode())
                .name(e.getName())
                .build()).toList();
    }

    @GetMapping("/enquire/{bankCode}/account/{account}")
    public NameEnquiryResponse getAccountName(@PathVariable("bankCode")String bankCode, @PathVariable("account")String account ){
        AccountEnquiryResponse response = paystackService.enquiryAccount(account,bankCode);
        return NameEnquiryResponse.builder().accountName(response.getAccountName()).build();
    }

    @PostMapping
    public BankAccountResponse addBankAccount(@RequestBody AddBankAccountRequest request){
        SnapUser user = securityUtil.getCurrentLoggedInUser();
        Business business = businessService.getBusinessOfUser(user);
        BankAccount account = bankAccountService.save(BankAccountDto.builder()
                        .accountNumber(request.getAccountNumber())
                        .bankName(request.getBankName())
                        .accountName(request.getAccountName())
                        .bankCode(request.getBankCode())
                        .build(),business);
        return BankAccountResponse.builder().bankAccount(account).build();
    }

    @GetMapping
    public List<BankAccountResponse> getBankAccounts(){
        SnapUser user = securityUtil.getCurrentLoggedInUser();
        Business business = businessService.getBusinessOfUser(user);
        return bankAccountService.get(business.getCode()).stream().map(e->BankAccountResponse.builder().bankAccount(e).build()).toList();
    }
}
