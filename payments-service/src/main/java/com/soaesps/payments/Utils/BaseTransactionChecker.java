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

import jakarta.annotation.Nonnull;
import javax.crypto.Cipher;
import java.math.BigDecimal;
import java.security.PrivateKey;
import java.security.Signature;
import java.util.Arrays;

/**
 * Utility class for cryptographic verification and generation of transaction/check entities.
 */
public class BaseTransactionChecker {

    // FIXED: Changed to public static final and uppercase to follow Java constant conventions.
    public static final String ISSUER_ID = "Internet Banking Open Source Organization";

    /**
     * Signs a check descriptor using the payer's private key and encrypts it for the payee.
     */
    public static byte[] signCheck(@Nonnull final CheckDesc checkDesc, @Nonnull final PrivateKey privateKey) throws Exception {
        // FIXED: Explicitly defined transformation. "RSA" alone is ambiguous and may fail or use insecure defaults in modern JCE.
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, CryptoHelper.bytesToPublicKey(checkDesc.getPayeePublicKey()));

        Signature privateSignature = Signature.getInstance("SHA256withRSA");
        privateSignature.initSign(privateKey);

        // WARNING: RSA can only encrypt small amounts of data (key_size_bytes - 11).
        // If serialized checkDesc exceeds this limit, IllegalBlockSizeException will be thrown.
        privateSignature.update(cipher.doFinal(CryptoHelper.serializeObject(checkDesc)));
        return privateSignature.sign();
    }

    /**
     * Verifies the signature of a check descriptor using the payer's public key.
     */
    public static boolean verifyCheck(@Nonnull final CheckDesc checkDesc, @Nonnull final byte[] signature) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, CryptoHelper.bytesToPublicKey(checkDesc.getPayeePublicKey()));

        Signature publicSignature = Signature.getInstance("SHA256withRSA");
        publicSignature.initVerify(CryptoHelper.bytesToPublicKey(checkDesc.getPayerPublicKey()));
        publicSignature.update(cipher.doFinal(CryptoHelper.serializeObject(checkDesc)));
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
        Signature publicSignature = Signature.getInstance("SHA256withRSA");
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
        clientBillDesc.setEncodedSharedSecret(clientBill.getServerBillDesc().getSharedSecret());

        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        // Assuming clientBillDesc.getPublicKey() returns the server's public key intended for the client
        cipher.init(Cipher.ENCRYPT_MODE, CryptoHelper.bytesToPublicKey(clientBillDesc.getPublicKey()));

        Signature privateSignature = Signature.getInstance("SHA256withRSA");
        privateSignature.initSign(CryptoHelper.bytesToPrivateKey(clientBill.getServerBillDesc().getPrivateKey()));
        privateSignature.update(cipher.doFinal(CryptoHelper.serializeObject(clientBillDesc)));

        BaseClientBill result = new BaseClientBill();
        clientBillDesc.setEncodedSharedSecret(""); // Clear sensitive data before returning
        result.setCreationTime(DateTimeHelper.getCurrentTimeWithTimeZone("UTC"));
        result.setClientBillDesc(clientBillDesc);
        result.setBillSignature(privateSignature.sign());
        return result;
    }

    /**
     * Transfers money between two server bills and generates a new client bill for the payer.
     * Note: This method lacks @Transactional annotation. Balance updates won't be persisted
     * automatically unless called within an active Spring transaction context.
     */
    public static BaseClientBill transferMoney(@Nonnull final BaseServerBill payerBill, @Nonnull final BaseServerBill payeeBill, final BigDecimal amount) throws Exception {
        BigDecimal payerBalance = payerBill.getServerBillDesc().getAccountBalance();
        BigDecimal payeeBalance = payeeBill.getServerBillDesc().getAccountBalance();

        if (payerBalance.compareTo(amount) >= 0) {
            payerBill.getServerBillDesc().setAccountBalance(payerBalance.subtract(amount));
            payeeBill.getServerBillDesc().setAccountBalance(payeeBalance.add(amount));
        } else {
            // TODO: Consider throwing a custom InsufficientFundsException instead of silently failing
        }

        return generateClientBill(payerBill);
    }
}