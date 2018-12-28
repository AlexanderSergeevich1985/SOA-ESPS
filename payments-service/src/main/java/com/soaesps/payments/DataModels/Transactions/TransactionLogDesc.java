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
package com.soaesps.payments.DataModels.Transactions;

import org.hibernate.validator.constraints.Length;

import javax.annotation.Nonnull;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Lob;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;

@Embeddable
public class TransactionLogDesc implements Serializable {
    @Column(name = "completion_time", nullable = false)
    private Timestamp completionTime;

    @Column(name = "payer_id", nullable = false)
    @Length(max = 500)
    private String payerId;

    @Column(name = "payee_id", nullable = false)
    @Length(max = 500)
    private String payeeId;

    @Column(name = "payer_balance", nullable = false)
    private BigDecimal payerBalance;

    @Column(name = "payee_balance", nullable = false)
    private BigDecimal payeeBalance;

    @Column(name = "transfer_amount", nullable = false)
    private BigDecimal transferAmount;

    @Lob
    @Column(name = "public_key", nullable = false)
    private byte[] publicKey;

    @Lob
    @Column(name = "private_key", nullable = false)
    private byte[] privateKey;

    public TransactionLogDesc() {}

    @Nonnull
    public Timestamp getCompletionTime() {
        return completionTime;
    }

    public void setCompletionTime(@Nonnull Timestamp completionTime) {
        this.completionTime = completionTime;
    }

    @Nonnull
    public String getPayerId() {
        return payerId;
    }

    public void setPayerId(@Nonnull String payerId) {
        this.payerId = payerId;
    }

    @Nonnull
    public String getPayeeId() {
        return payeeId;
    }

    public void setPayeeId(@Nonnull String payeeId) {
        this.payeeId = payeeId;
    }

    @Nonnull
    public BigDecimal getPayerBalance() {
        return payerBalance;
    }

    public void setPayerBalance(@Nonnull BigDecimal payerBalance) {
        this.payerBalance = payerBalance;
    }

    @Nonnull
    public BigDecimal getPayeeBalance() {
        return payeeBalance;
    }

    public void setPayeeBalance(@Nonnull BigDecimal payeeBalance) {
        this.payeeBalance = payeeBalance;
    }

    @Nonnull
    public BigDecimal getTransferAmount() {
        return transferAmount;
    }

    public void setTransferAmount(@Nonnull BigDecimal transferAmount) {
        this.transferAmount = transferAmount;
    }

    @Nonnull
    public byte[] getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(@Nonnull byte[] publicKey) {
        this.publicKey = publicKey;
    }

    @Nonnull
    public byte[] getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(@Nonnull byte[] privateKey) {
        this.privateKey = privateKey;
    }
}