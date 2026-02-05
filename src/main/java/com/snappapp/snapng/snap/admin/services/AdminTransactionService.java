package com.snappapp.snapng.snap.admin.services;

import com.snappapp.snapng.snap.admin.apimodels.TransactionApiResponse;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

public interface AdminTransactionService {
    Page<TransactionApiResponse> getAll(Integer page, Integer size);

    TransactionApiResponse getById(Long id);
}
