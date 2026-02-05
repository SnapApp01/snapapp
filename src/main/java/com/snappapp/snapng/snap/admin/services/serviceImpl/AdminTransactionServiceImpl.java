package com.snappapp.snapng.snap.admin.services.serviceImpl;

import com.snappapp.snapng.exceptions.ResourceNotFoundException;
import com.snappapp.snapng.snap.admin.apimodels.TransactionApiResponse;
import com.snappapp.snapng.snap.admin.services.AdminTransactionService;
import com.snappapp.snapng.snap.data_lib.entities.WalletTransfer;
import com.snappapp.snapng.snap.data_lib.enums.TransferStatus;
import com.snappapp.snapng.snap.data_lib.repositories.WalletTransferRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AdminTransactionServiceImpl implements AdminTransactionService {

    private final WalletTransferRepository walletTransferRepository;

    public AdminTransactionServiceImpl(WalletTransferRepository walletTransferRepository) {
        this.walletTransferRepository = walletTransferRepository;
    }

    @Override
    public Page<TransactionApiResponse> getAll(Integer page, Integer size) {

        log.info("[ADMIN_TX] Fetching wallet transfers. page={}, size={}", page, size);

        Page<WalletTransfer> transfers =
                walletTransferRepository.findAll(
                        PageRequest.of(
                                page,
                                size,
                                Sort.by(Sort.Direction.DESC, "createdAt")
                        )
                );

        return transfers.map(this::mapToResponse);
    }

    @Override
    public TransactionApiResponse getById(Long id) {

        log.info("[ADMIN_TX] Fetching wallet transfer. id={}", id);

        WalletTransfer transfer =
                walletTransferRepository.findById(id)
                        .orElseThrow(() -> {
                            log.warn("[ADMIN_TX] Wallet transfer not found. id={}", id);
                            return new ResourceNotFoundException("Transaction not found");
                        });

        return mapToResponse(transfer);
    }

    private TransactionApiResponse mapToResponse(WalletTransfer transfer) {

        log.debug(
                "[ADMIN_TX_MAP] transferId={}, debitWallet={}, creditWallet={}, amount={}, debitStatus={}, creditStatus={}",
                transfer.getId(),
                transfer.getDebitWallet(),
                transfer.getCreditWallet(),
                transfer.getAmount(),
                transfer.getDebitStatus(),
                transfer.getCreditStatus()
        );

        /*
         * IMPORTANT:
         * A WalletTransfer represents one escrow movement (not a single wallet leg).
         * For admin view we expose it as ONE transaction row.
         *
         * We treat it as a CREDIT transaction on the recipient side by default.
         */

        return TransactionApiResponse.builder()
                .id(String.valueOf(transfer.getId()))
                .email(null)                 // Not available from WalletTransfer
                .ownerType(null)             // Not available from WalletTransfer
                .isDebit(false)              // admin row shown as credit-side movement
                .amount(transfer.getAmount())
                .description(transfer.getNarration())
                .owner(transfer.getCreditWallet())
                .time(
                        transfer.getCreatedAt() != null
                                ? transfer.getCreatedAt().toLocalTime()
                                : null
                )
                .date(
                        transfer.getCreatedAt() != null
                                ? transfer.getCreatedAt().toLocalDate()
                                : null
                )
                .build();
    }

    @SuppressWarnings("unused")
    private String resolveStatus(WalletTransfer transfer) {

        if (TransferStatus.REVERSED.equals(transfer.getDebitStatus())
                || TransferStatus.REVERSED.equals(transfer.getCreditStatus())) {
            return "REVERSED";
        }

        if (TransferStatus.COMPLETE.equals(transfer.getDebitStatus())
                && TransferStatus.COMPLETE.equals(transfer.getCreditStatus())) {
            return "COMPLETED";
        }

        return "PENDING";
    }
}
