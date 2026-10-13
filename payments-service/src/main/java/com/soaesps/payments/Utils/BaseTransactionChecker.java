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
package com.soaesps.payments.Utils;

import com.soaesps.core.Utils.CryptoHelper;
import com.soaesps.core.Utils.DateTimeHelper;
import com.soaesps.payments.DataModels.Transactions.*;

import com.soaesps.payments.dto.TransactionDto;
import jakarta.annotation.Nonnull;

import java.math.BigDecimal;
import java.security.PrivateKey;
import java.security.Signature;
import java.util.Arrays;
import java.util.Optional;

/**
 * Utility class for cryptographic verification and generation of transaction/check entities.
 */
public class BaseTransactionChecker {
    public static final String ISSUER_ID = "Internet Banking Open Source Organization";
    private static final String SIGNATURE_ALGORITHM = "SHA256withRSA";

    /**
     * Signs a check descriptor using the payer's private key and encrypts it for the payee.
     */
    public static byte[] signCheck(@Nonnull final CheckDesc checkDesc, @Nonnull final PrivateKey privateKey) throws Exception {
        Signature privateSignature = Signature.getInstance(SIGNATURE_ALGORITHM);
        privateSignature.initSign(privateKey);

        // Sign the serialized bytes of the check description directly
        privateSignature.update(CryptoHelper.serializeObject(checkDesc));
        return privateSignature.sign();
    }

    /**
     * Verifies the signature of a check descriptor using the payer's public key.
     * Fixed: Removed improper Cipher ENCRYPT_MODE usage from the verification phase.
     */
    public static boolean verifyCheck(@Nonnull final CheckDesc checkDesc, @Nonnull final byte[] signature) throws Exception {
        Signature publicSignature = Signature.getInstance(SIGNATURE_ALGORITHM);
        publicSignature.initVerify(CryptoHelper.bytesToPublicKey(checkDesc.getPayerPublicKey()));

        publicSignature.update(CryptoHelper.serializeObject(checkDesc));
        return publicSignature.verify(signature);
    }

    /**
     * Verifies a check against the stored server bills of the payer and payee.
     */
    public static boolean verifyCheck(@Nonnull final BaseCheck check, @Nonnull final BaseServerBill payerBill, @Nonnull final BaseServerBill payeeBill) throws Exception {
        if (!Arrays.equals(check.getCheckDesc().getPayerPublicKey(), payerBill.getServerBillDesc().getOwnerPublicKey())) return false;
        if (!Arrays.equals(check.getCheckDesc().getPayeePublicKey(), payeeBill.getServerBillDesc().getOwnerPublicKey())) return false;
        return verifyCheck(check.getCheckDesc(), check.getCheckSignature());
    }

    /**
     * Verifies the signature of a transaction descriptor.
     */
    public static boolean verifyTransaction(@Nonnull final TransactionDesc transactionDesc, @Nonnull final byte[] signature) throws Exception {
        Signature publicSignature = Signature.getInstance(SIGNATURE_ALGORITHM);
        publicSignature.initVerify(CryptoHelper.bytesToPublicKey(transactionDesc.getPayerPublicKey()));
        publicSignature.update(CryptoHelper.serializeObject(transactionDesc));
        return publicSignature.verify(signature);
    }

    /**
     * Verifies a transaction against the payer's stored server bill.
     */
    public static boolean verifyTransaction(@Nonnull final BaseTransaction transaction, @Nonnull final BaseServerBill payerBill) throws Exception {
        if (!Arrays.equals(transaction.getTransactionDescriptor().getPayerPublicKey(), payerBill.getServerBillDesc().getOwnerPublicKey())) return false;
        return verifyTransaction(transaction.getTransactionDescriptor(), transaction.getTransactionSignature());
    }

    /**
     * Generates a client-facing bill from the internal server bill, signing it with the server's private key.
     */
    public static BaseClientBill generateClientBill(@Nonnull final BaseServerBill clientBill) throws Exception {
        ClientBillDesc clientBillDesc = new ClientBillDesc();
        clientBillDesc.setAccountBalance(clientBill.getServerBillDesc().getAccountBalance());
        clientBillDesc.setUuid(clientBill.getServerBillDesc().getUuid());
        clientBillDesc.setIssuerId(ISSUER_ID);
        clientBillDesc.setServerPublicKey(clientBill.getServerBillDesc().getPublicKey());

        // Create the digital signature of the server over the public descriptor
        Signature privateSignature = Signature.getInstance(SIGNATURE_ALGORITHM);
        privateSignature.initSign(CryptoHelper.bytesToPrivateKey(clientBill.getServerBillDesc().getPrivateKey()));
        privateSignature.update(CryptoHelper.serializeObject(clientBillDesc));
        byte[] billSignature = privateSignature.sign();

        BaseClientBill result = new BaseClientBill();
        result.setCreationTime(DateTimeHelper.getCurrentTimeWithTimeZone("UTC"));
        result.setClientBillDesc(clientBillDesc);
        result.setBillSignature(billSignature);
        return result;
    }

    /**
     * Transfers money between two server bills and generates a new client bill for the payer.
     */
    public static Optional<BaseClientBill> transferMoney(@Nonnull final BaseServerBill payerBill, @Nonnull final BaseServerBill payeeBill, final BigDecimal amount) throws Exception {
        BigDecimal payerBalance = payerBill.getServerBillDesc().getAccountBalance();
        BigDecimal payeeBalance = payeeBill.getServerBillDesc().getAccountBalance();

        if (payerBalance.compareTo(amount) < 0) {
            throw new IllegalArgumentException("Transfer aborted: Insufficient funds on payer balance.");
        }

        payerBill.getServerBillDesc().setAccountBalance(payerBalance.subtract(amount));
        payeeBill.getServerBillDesc().setAccountBalance(payeeBalance.add(amount));

        return Optional.of(generateClientBill(payerBill));
    }
}