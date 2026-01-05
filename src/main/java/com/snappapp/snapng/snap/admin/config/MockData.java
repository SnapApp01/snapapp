package com.snappapp.snapng.snap.admin.config;

import com.snappapp.snapng.snap.admin.apimodels.*;
import com.snappapp.snapng.snap.data_lib.enums.SnapUserType;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

@Component
@Getter
@Slf4j
public class MockData {
    private Map<String, UserApiResponse> userList = new HashMap<>();
    private Map<String, PartnerApiResponse> partnerList = new HashMap<>();
    private Map<String, TransactionApiResponse> transactionList = new HashMap<>();
    private Map<String, DeliveryRequestApiResponse> requestList = new HashMap<>();
    private Map<String, VehicleApiResponse> vehicleList = new HashMap<>();
    @PostConstruct
    public void init(){
        prefillTransactions();
        prefillRequests();
        prefillVehicles();
        prefillPartners();
        prefillUsers();
    }

    void prefillVehicles(){
        vehicleList.put("v1", VehicleApiResponse
                .builder()
                .enabled(false)
                .id("v1")
                .owner("G&H Limited")
                .plateNumber("ABC12XY")
                .type("VAN")
                .build());
        vehicleList.put("v2", VehicleApiResponse
                .builder()
                .enabled(false)
                .id("v2")
                .owner("G&H Limited")
                .plateNumber("TYU43XY")
                .type("BIKE")
                .build());
    }

    void prefillRequests(){
        requestList.put("aa", DeliveryRequestApiResponse
                .builder()
                .currentPosition("IBADAN")
                .customerName("Abdulgafar Obeitor")
                .customerPhone("08167024199")
                .description("Test Goods")
                .endAddress("Dawaki Hillside Estate, Abuja")
                .startAddress("Eddie Eleje, Silverspring Estate")
                .fee(200000L)
                .frequency(1)
                .id("aa")
                .recipientName("Tunde")
                .recipientPhone("080012345678")
                .status("PENDING")
                .vehicleId("v1")
                .vehiclePlate("ABC12XY")
                .vehicleType("VAN")
                .worthOfGood(2500000L)
                .build());
    }

    void prefillTransactions(){
        transactionList.put("t1", TransactionApiResponse
                .builder()
                .amount(5000L)
                .date(LocalDate.now().minusDays(2))
                .description("Wallet Funding")
                .email("aobeitor@yahoo.com")
                .id("t1")
                .isDebit(false)
                .owner("Abdulgafar Obeitor")
                .ownerType(SnapUserType.USER)
                .time(LocalTime.now())
                .build());
        transactionList.put("t2", TransactionApiResponse
                .builder()
                .amount(25000L)
                .date(LocalDate.now())
                .description("Withdrawal from Wallet")
                .email("obt4lyfe@gmail.com")
                .id("t2")
                .isDebit(true)
                .owner("Onimisi Obeitor")
                .ownerType(SnapUserType.BUSINESS)
                .time(LocalTime.now().minusHours(5))
                .build());
    }

    void prefillPartners(){
        partnerList.put("partnerId1",PartnerApiResponse
                .builder()
                .blocked(false)
                .id("partnerId1")
                .company("G&H Limited")
                .email("aobeitor@yahoo.com")
                .firstname("Abdulgafar")
                .lastname("Obeitor")
                .phoneNumber("08167024199")
                .registeredOn(LocalDateTime.now().minus(63, ChronoUnit.HOURS))
                .walletBalance(5100L)
                .build());
        partnerList.put("7sMcGQjWJjc4AO4V6IhEQlG8dWT2", PartnerApiResponse
                .builder()
                .company("Gabiitech Solutions")
                .blocked(false)
                .email("la272legath@gmail.com")
                .firstname("Gabriel")
                .lastname("Ogun")
                .id("7sMcGQjWJjc4AO4V6IhEQlG8dWT2")
                .phoneNumber("")
                .registeredOn(LocalDateTime.now())
                .verified(false)
                .walletBalance(0L)
                .build());
        partnerList.put("R2mc3qsWRgaxvgP8jyFBPsotYEC2", PartnerApiResponse
                .builder()
                .company("enekwalimited")
                .blocked(false)
                .email("millercherriec@gmail.com")
                .firstname("Cherrie")
                .lastname("Enekwa")
                .id("R2mc3qsWRgaxvgP8jyFBPsotYEC2")
                .phoneNumber("")
                .registeredOn(LocalDateTime.now())
                .verified(false)
                .walletBalance(0L)
                .build());
    }

    void prefillUsers(){
        userList.put("userid1",UserApiResponse
                .builder()
                .blocked(false)
                .id("userid1")
                .email("aobeitor@yahoo.com")
                .firstname("Abdulgafar")
                .lastname("Obeitor")
                .phoneNumber("08167024199")
                .registeredOn(LocalDateTime.now().minus(63, ChronoUnit.HOURS))
                .requestCount(5)
                .walletBalance(5100L)
                .build());
        userList.put("userid2",UserApiResponse
                .builder()
                .blocked(false)
                .id("userid2")
                .email("obt4lyfe@gmail.com")
                .firstname("Onimisi")
                .lastname("Obeitor")
                .phoneNumber("08167024199")
                .registeredOn(LocalDateTime.now().minus(18, ChronoUnit.HOURS))
                .requestCount(2)
                .walletBalance(2300L)
                .build());
    }
}
