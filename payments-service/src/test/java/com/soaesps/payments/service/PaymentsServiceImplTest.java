package com.soaesps.payments.service;

import com.soaesps.payments.DataModels.Transactions.*;
import com.soaesps.payments.Utils.BaseTransactionChecker;
import com.soaesps.payments.repository.ServerBillsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link PaymentsServiceImpl}.
 * Uses Mockito 5+ mockStatic to mock static utility methods of BaseTransactionChecker.
 */
@ExtendWith(MockitoExtension.class)
class PaymentsServiceImplTest {

    @Mock
    private ServerBillsRepository repository;

    private PaymentsServiceImpl paymentsService;

    private BaseServerBill payerBill;
    private BaseServerBill payeeBill;
    private TransactionDesc transactionDesc;
    private CheckDesc checkDesc;
    private BaseClientBill expectedClientBill;

    @BeforeEach
    void setUp() {
        paymentsService = new PaymentsServiceImpl(repository);

        payerBill = mock(BaseServerBill.class);
        payeeBill = mock(BaseServerBill.class);

        transactionDesc = mock(TransactionDesc.class);
        when(transactionDesc.getPayerId()).thenReturn("payer-1");
        when(transactionDesc.getPayeeId()).thenReturn("payee-1");
        when(transactionDesc.getTransferAmount()).thenReturn(new BigDecimal("100.00"));

        checkDesc = mock(CheckDesc.class);
        when(checkDesc.getPayerId()).thenReturn("payer-1");
        when(checkDesc.getPayeeId()).thenReturn("payee-1");
        when(checkDesc.getTransferAmount()).thenReturn(new BigDecimal("50.00"));

        expectedClientBill = mock(BaseClientBill.class);
    }

    @Nested
    @DisplayName("transferMoney")
    class TransferMoneyTests {

        @Test
        @DisplayName("Should return updated client bill when transaction is verified and transfer succeeds")
        void transferMoney_success() {
            BaseTransaction transaction = mock(BaseTransaction.class);
            when(transaction.getTransactionDescriptor()).thenReturn(transactionDesc);

            when(repository.findById("payer-1")).thenReturn(Optional.of(payerBill));
            when(repository.findById("payee-1")).thenReturn(Optional.of(payeeBill));

            try (MockedStatic<BaseTransactionChecker> mockedChecker = mockStatic(BaseTransactionChecker.class)) {
                mockedChecker.when(() -> BaseTransactionChecker.verifyTransaction(transaction, payerBill))
                        .thenReturn(true);
                mockedChecker.when(() -> BaseTransactionChecker.transferMoney(
                                eq(payerBill), eq(payeeBill), eq(new BigDecimal("100.00"))))
                        .thenReturn(expectedClientBill);

                BaseClientBill result = paymentsService.transferMoney(transaction);

                assertThat(result).isSameAs(expectedClientBill);
                mockedChecker.verify(() -> BaseTransactionChecker.verifyTransaction(transaction, payerBill));
                mockedChecker.verify(() -> BaseTransactionChecker.transferMoney(
                        payerBill, payeeBill, new BigDecimal("100.00")));
            }
        }

        @Test
        @DisplayName("Should return null when payer bill is not found")
        void transferMoney_payerNotFound() {
            BaseTransaction transaction = mock(BaseTransaction.class);
            when(transaction.getTransactionDescriptor()).thenReturn(transactionDesc);

            when(repository.findById("payer-1")).thenReturn(Optional.empty());
            when(repository.findById("payee-1")).thenReturn(Optional.of(payeeBill));

            try (MockedStatic<BaseTransactionChecker> mockedChecker = mockStatic(BaseTransactionChecker.class)) {
                BaseClientBill result = paymentsService.transferMoney(transaction);

                assertThat(result).isNull();

                mockedChecker.verify(() -> BaseTransactionChecker.verifyTransaction(
                        any(BaseTransaction.class), any(BaseServerBill.class)), never());
                mockedChecker.verify(() -> BaseTransactionChecker.transferMoney(
                        any(BaseServerBill.class), any(BaseServerBill.class), any(BigDecimal.class)), never());
            }
        }

        @Test
        @DisplayName("Should return null when payee bill is not found")
        void transferMoney_payeeNotFound() {
            BaseTransaction transaction = mock(BaseTransaction.class);
            when(transaction.getTransactionDescriptor()).thenReturn(transactionDesc);

            when(repository.findById("payer-1")).thenReturn(Optional.of(payerBill));
            when(repository.findById("payee-1")).thenReturn(Optional.empty());

            try (MockedStatic<BaseTransactionChecker> mockedChecker = mockStatic(BaseTransactionChecker.class)) {
                BaseClientBill result = paymentsService.transferMoney(transaction);

                assertThat(result).isNull();

                mockedChecker.verify(() -> BaseTransactionChecker.verifyTransaction(
                        any(BaseTransaction.class), any(BaseServerBill.class)), never());
            }
        }

        @Test
        @DisplayName("Should return null when cryptographic verification fails")
        void transferMoney_verificationFails() {
            BaseTransaction transaction = mock(BaseTransaction.class);
            when(transaction.getTransactionDescriptor()).thenReturn(transactionDesc);

            when(repository.findById("payer-1")).thenReturn(Optional.of(payerBill));
            when(repository.findById("payee-1")).thenReturn(Optional.of(payeeBill));

            try (MockedStatic<BaseTransactionChecker> mockedChecker = mockStatic(BaseTransactionChecker.class)) {
                mockedChecker.when(() -> BaseTransactionChecker.verifyTransaction(transaction, payerBill))
                        .thenReturn(false);

                BaseClientBill result = paymentsService.transferMoney(transaction);

                assertThat(result).isNull();

                mockedChecker.verify(() -> BaseTransactionChecker.transferMoney(
                        any(BaseServerBill.class), any(BaseServerBill.class), any(BigDecimal.class)), never());
            }
        }

        @Test
        @DisplayName("Should return null and not propagate exception when transfer throws")
        void transferMoney_exceptionDuringTransfer() {
            BaseTransaction transaction = mock(BaseTransaction.class);
            when(transaction.getTransactionDescriptor()).thenReturn(transactionDesc);

            when(repository.findById("payer-1")).thenReturn(Optional.of(payerBill));
            when(repository.findById("payee-1")).thenReturn(Optional.of(payeeBill));

            try (MockedStatic<BaseTransactionChecker> mockedChecker = mockStatic(BaseTransactionChecker.class)) {
                mockedChecker.when(() -> BaseTransactionChecker.verifyTransaction(transaction, payerBill))
                        .thenReturn(true);
                mockedChecker.when(() -> BaseTransactionChecker.transferMoney(
                                any(BaseServerBill.class), any(BaseServerBill.class), any(BigDecimal.class)))
                        .thenThrow(new RuntimeException("Crypto failure"));

                BaseClientBill result = paymentsService.transferMoney(transaction);

                assertThat(result).isNull();
            }
        }
    }

    @Nested
    @DisplayName("cashCheck")
    class CashCheckTests {

        @Test
        @DisplayName("Should transfer funds when check verification succeeds")
        void cashCheck_success() {
            BaseCheck check = mock(BaseCheck.class);
            when(check.getCheckDesc()).thenReturn(checkDesc);

            when(repository.findById("payer-1")).thenReturn(Optional.of(payerBill));
            when(repository.findById("payee-1")).thenReturn(Optional.of(payeeBill));

            try (MockedStatic<BaseTransactionChecker> mockedChecker = mockStatic(BaseTransactionChecker.class)) {
                mockedChecker.when(() -> BaseTransactionChecker.verifyCheck(check, payerBill, payeeBill))
                        .thenReturn(true);
                mockedChecker.when(() -> BaseTransactionChecker.transferMoney(
                                eq(payerBill), eq(payeeBill), eq(new BigDecimal("50.00"))))
                        .thenReturn(expectedClientBill);

                BaseClientBill result = paymentsService.cashCheck(check);

                assertThat(result).isSameAs(expectedClientBill);
                mockedChecker.verify(() -> BaseTransactionChecker.verifyCheck(check, payerBill, payeeBill));
            }
        }

        @Test
        @DisplayName("Should return null when payer bill is missing")
        void cashCheck_payerNotFound() {
            BaseCheck check = mock(BaseCheck.class);
            when(check.getCheckDesc()).thenReturn(checkDesc);

            when(repository.findById("payer-1")).thenReturn(Optional.empty());

            try (MockedStatic<BaseTransactionChecker> mockedChecker = mockStatic(BaseTransactionChecker.class)) {
                BaseClientBill result = paymentsService.cashCheck(check);

                assertThat(result).isNull();

                mockedChecker.verify(() -> BaseTransactionChecker.verifyCheck(
                        any(BaseCheck.class), any(BaseServerBill.class), any(BaseServerBill.class)), never());
            }
        }

        @Test
        @DisplayName("Should return null when check verification fails")
        void cashCheck_verificationFails() {
            BaseCheck check = mock(BaseCheck.class);
            when(check.getCheckDesc()).thenReturn(checkDesc);

            when(repository.findById("payer-1")).thenReturn(Optional.of(payerBill));
            when(repository.findById("payee-1")).thenReturn(Optional.of(payeeBill));

            try (MockedStatic<BaseTransactionChecker> mockedChecker = mockStatic(BaseTransactionChecker.class)) {
                mockedChecker.when(() -> BaseTransactionChecker.verifyCheck(check, payerBill, payeeBill))
                        .thenReturn(false);

                BaseClientBill result = paymentsService.cashCheck(check);

                assertThat(result).isNull();

                mockedChecker.verify(() -> BaseTransactionChecker.transferMoney(
                        any(BaseServerBill.class), any(BaseServerBill.class), any(BigDecimal.class)), never());
            }
        }

        @Test
        @DisplayName("Should swallow exceptions and return null")
        void cashCheck_exceptionHandled() {
            BaseCheck check = mock(BaseCheck.class);
            when(check.getCheckDesc()).thenReturn(checkDesc);

            when(repository.findById("payer-1")).thenReturn(Optional.of(payerBill));
            when(repository.findById("payee-1")).thenReturn(Optional.of(payeeBill));

            try (MockedStatic<BaseTransactionChecker> mockedChecker = mockStatic(BaseTransactionChecker.class)) {
                mockedChecker.when(() -> BaseTransactionChecker.verifyCheck(check, payerBill, payeeBill))
                        .thenThrow(new RuntimeException("Signature invalid"));

                BaseClientBill result = paymentsService.cashCheck(check);

                assertThat(result).isNull();
            }
        }
    }

    @Nested
    @DisplayName("refreshBill")
    class RefreshBillTests {

        @Test
        @DisplayName("Should generate new client bill when server bill exists")
        void refreshBill_success() {
            when(repository.findById("bill-1")).thenReturn(Optional.of(payerBill));

            try (MockedStatic<BaseTransactionChecker> mockedChecker = mockStatic(BaseTransactionChecker.class)) {
                mockedChecker.when(() -> BaseTransactionChecker.generateClientBill(payerBill))
                        .thenReturn(expectedClientBill);

                BaseClientBill result = paymentsService.refreshBill("bill-1");

                assertThat(result).isSameAs(expectedClientBill);
            }
        }

        @Test
        @DisplayName("Should return null when bill is not found")
        void refreshBill_notFound() {
            when(repository.findById("bill-1")).thenReturn(Optional.empty());

            BaseClientBill result = paymentsService.refreshBill("bill-1");

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("Should return null when generation throws exception")
        void refreshBill_exceptionHandled() {
            when(repository.findById("bill-1")).thenReturn(Optional.of(payerBill));

            try (MockedStatic<BaseTransactionChecker> mockedChecker = mockStatic(BaseTransactionChecker.class)) {
                mockedChecker.when(() -> BaseTransactionChecker.generateClientBill(payerBill))
                        .thenThrow(new RuntimeException("Key expired"));

                BaseClientBill result = paymentsService.refreshBill("bill-1");

                assertThat(result).isNull();
            }
        }
    }
}