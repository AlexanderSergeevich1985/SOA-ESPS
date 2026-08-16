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
import com.soaesps.payments.repository.ServerBillsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service implementation for processing payments, checks, and bill generation.
 */
@Service
public class PaymentsServiceImpl implements PaymentsService {
    private static final Logger logger = LoggerFactory.getLogger(PaymentsServiceImpl.class);

    private final ServerBillsRepository repository;

    public PaymentsServiceImpl(ServerBillsRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public BaseClientBill transferMoney(final BaseTransaction transaction) {
        // FIXED: findOne() was removed in Spring Data JPA 3.0. Replaced with findById().orElse(null)
        BaseServerBill payerBill = this.repository.findById(transaction.getTransactionDescriptor().getPayerId()).orElse(null);
        BaseServerBill payeeBill = this.repository.findById(transaction.getTransactionDescriptor().getPayeeId()).orElse(null);

        if (payerBill == null || payeeBill == null) {
            logger.warn("Transfer failed: Payer or Payee bill not found for transaction");
            return null;
        }

        try {
            if (BaseTransactionChecker.verifyTransaction(transaction, payerBill)) {
                return BaseTransactionChecker.transferMoney(payerBill, payeeBill, transaction.getTransactionDescriptor().getTransferAmount());
            } else {
                logger.warn("Transfer failed: Cryptographic verification failed for transaction");
            }
        } catch (final Exception ex) {
            // Using SLF4J parameterized logging instead of java.util.logging
            logger.error("Exception occurred during money transfer", ex);
        }

        return null;
    }

    public BaseClientBill cashCheck(final BaseCheck check) {
        BaseServerBill payerBill = this.repository.findById(check.getCheckDesc().getPayerId()).orElse(null);
        BaseServerBill payeeBill = this.repository.findById(check.getCheckDesc().getPayeeId()).orElse(null);

        if (payerBill == null || payeeBill == null) {
            logger.warn("Check cashing failed: Payer or Payee bill not found");
            return null;
        }

        try {
            if (BaseTransactionChecker.verifyCheck(check, payerBill, payeeBill)) {
                return BaseTransactionChecker.transferMoney(payerBill, payeeBill, check.getCheckDesc().getTransferAmount());
            } else {
                logger.warn("Check cashing failed: Cryptographic verification failed");
            }
        } catch (final Exception ex) {
            logger.error("Exception occurred during check cashing", ex);
        }

        return null;
    }

    @Override
    public BaseClientBill refreshBill(String billId) {
        BaseServerBill clientBill = this.repository.findById(billId).orElse(null);
        if (clientBill == null) {
            logger.warn("Bill refresh failed: Bill with id {} not found", billId);
            return null;
        }

        try {
            return BaseTransactionChecker.generateClientBill(clientBill);
        } catch (final Exception ex) {
            logger.error("Exception occurred during bill refresh for id {}", billId, ex);
        }

        return null;
    }
}