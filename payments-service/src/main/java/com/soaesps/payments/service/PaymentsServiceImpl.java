/**The MIT License (MIT)
 Copyright (c) 2018 by AleksanderSergeevich
 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:
 The above copyright notice and this permission notice shall be included in all
 copies or substantial portions of the Software.
 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 SOFTWARE.
 */
package com.soaesps.payments.service;

import com.soaesps.payments.DataModels.Transactions.BaseCheck;
import com.soaesps.payments.DataModels.Transactions.BaseClientBill;
import com.soaesps.payments.DataModels.Transactions.BaseServerBill;
import com.soaesps.payments.DataModels.Transactions.BaseTransaction;
import com.soaesps.payments.Utils.BaseTransactionChecker;
import com.soaesps.payments.dto.ClientBillDto;
import com.soaesps.payments.dto.TransactionDto;
import com.soaesps.payments.mapper.ClientBillMapper;
import com.soaesps.payments.mapper.TransactionMapper;
import com.soaesps.payments.repository.ServerBillsRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

/**
 * Service implementation for processing payments, checks, and bill generation.
 */
@Service
public class PaymentsServiceImpl implements PaymentsService {
    private static final Logger logger = LoggerFactory.getLogger(PaymentsServiceImpl.class);

    private final TransactionMapper transactionMapper;
    private final ClientBillMapper clientBillMapper;
    private final ServerBillsRepository repository;

    public PaymentsServiceImpl(TransactionMapper transactionMapper, ClientBillMapper clientBillMapper, ServerBillsRepository repository) {
        this.transactionMapper = transactionMapper;
        this.clientBillMapper = clientBillMapper;
        this.repository = repository;
    }

    @Override
    @Transactional
    public Optional<ClientBillDto> transferMoney(final TransactionDto dto) {
        BaseTransaction transaction = transactionMapper.toEntity(dto);
        UUID payerUuid = UUID.fromString(transaction.getTransactionDescriptor().getPayerId());
        UUID payeeUuid = UUID.fromString(transaction.getTransactionDescriptor().getPayerId());

        /*
         * To prevent Deadlocks, we must always acquire database locks in a strict deterministic order.
         * Sorting the UUIDs ensures that the 'SELECT FOR UPDATE' queries always execute in the exact same sequence.
         */
        UUID firstUuid = payerUuid.compareTo(payeeUuid) < 0 ? payerUuid : payeeUuid;
        UUID secondUuid = payerUuid.compareTo(payeeUuid) < 0 ? payeeUuid : payerUuid;

        BaseServerBill firstBill = this.repository.findAndLockByServerBillDesc_Uuid(firstUuid)
                .orElseThrow(() -> new IllegalArgumentException("Account not found for UUID: " + firstUuid));
        BaseServerBill secondBill = this.repository.findAndLockByServerBillDesc_Uuid(secondUuid)
                .orElseThrow(() -> new IllegalArgumentException("Account not found for UUID: " + secondUuid));

        // Safely re-assign variables back to their functional business roles
        BaseServerBill payerBill = payerUuid.equals(firstUuid) ? firstBill : secondBill;
        BaseServerBill payeeBill = payeeUuid.equals(firstUuid) ? firstBill : secondBill;

        try {
            if (BaseTransactionChecker.verifyTransaction(transaction, payerBill)) {
                BaseClientBill clientBill = BaseTransactionChecker
                        .transferMoney(payerBill, payeeBill, transaction.getTransactionDescriptor().getTransferAmount())
                        .orElseThrow(() -> new IllegalStateException("Failed to execute money transfer inside checker"));
                ClientBillDto dtoBill = clientBillMapper.toDto(clientBill);
                return Optional.of(dtoBill);
            } else {
                logger.warn("Transfer failed: Cryptographic verification failed for transaction");
            }
        } catch (final Exception ex) {
            logger.error("Exception occurred during money transfer", ex);
            throw new RuntimeException("Money transfer transaction failed due to internal error", ex);
        }

        return Optional.empty();
    }

    @Override
    @Transactional
    public Optional<ClientBillDto> cashCheck(final BaseCheck check) {
        UUID payerUuid = UUID.fromString(check.getCheckDesc().getPayerId());
        UUID payeeUuid = UUID.fromString(check.getCheckDesc().getPayeeId());

        /*
         * To prevent Deadlocks, we must always acquire database locks in a strict deterministic order.
         * Sorting the UUIDs ensures that the 'SELECT FOR UPDATE' queries always execute in the exact same sequence.
         */
        UUID firstUuid = payerUuid.compareTo(payeeUuid) < 0 ? payerUuid : payeeUuid;
        UUID secondUuid = payerUuid.compareTo(payeeUuid) < 0 ? payeeUuid : payerUuid;

        BaseServerBill firstBill = this.repository.findAndLockByServerBillDesc_Uuid(firstUuid)
                .orElseThrow(() -> new IllegalArgumentException("Account not found for UUID: " + firstUuid));
        BaseServerBill secondBill = this.repository.findAndLockByServerBillDesc_Uuid(secondUuid)
                .orElseThrow(() -> new IllegalArgumentException("Account not found for UUID: " + secondUuid));

        // Safely re-assign variables back to their functional business roles
        BaseServerBill payerBill = payerUuid.equals(firstUuid) ? firstBill : secondBill;
        BaseServerBill payeeBill = payeeUuid.equals(firstUuid) ? firstBill : secondBill;

        try {
            if (BaseTransactionChecker.verifyCheck(check, payerBill, payeeBill)) {
                BaseClientBill clientBill = BaseTransactionChecker
                        .transferMoney(payerBill, payeeBill, check.getCheckDesc().getTransferAmount())
                        .orElseThrow(() -> new IllegalStateException("Failed to execute money transfer inside checker"));
                ClientBillDto dtoBill = clientBillMapper.toDto(clientBill);
                return Optional.of(dtoBill);
            } else {
                logger.warn("Check cashing failed: Cryptographic verification failed");
            }
        } catch (final Exception ex) {
            logger.error("Exception occurred during check cashing", ex);
            throw new RuntimeException("Check cashing transaction failed due to internal error", ex);
        }

        return Optional.empty();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ClientBillDto> refreshBill(String billId) {
        // No pessimistic locking needed here since it is a pure read operation mapping to a DTO
        BaseServerBill clientBill = this.repository.findByServerBillDesc_Uuid(UUID.fromString(billId)).orElse(null);
        if (clientBill == null) {
            logger.warn("Bill refresh failed: Bill with id {} not found", billId);
            return Optional.empty();
        }

        try {
            ClientBillDto dtoBill = clientBillMapper.toDto(BaseTransactionChecker.generateClientBill(clientBill));
            return Optional.of(dtoBill);
        } catch (final Exception ex) {
            logger.error("Exception occurred during bill refresh for id {}", billId, ex);
        }

        return Optional.empty();
    }
}