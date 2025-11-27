package com.test.payment_pbls.dtos;

import java.util.List;

public record BatchResultDTO(
        int successCount,
        int failureCount,
        List<Long> failedInstructionIds
) {
}
