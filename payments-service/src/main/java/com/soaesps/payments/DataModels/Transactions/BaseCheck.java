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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.soaesps.core.DataModels.BaseEntity;

import javax.annotation.Nonnull;
import javax.persistence.*;

@Entity
@Table(name = "BASE_CHECK")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class BaseCheck extends BaseEntity {
    @Embedded
    private CheckDesc checkDesc;

    @Lob
    @Column(name = "check_signature", nullable = false)
    private byte[] checkSignature;

    public BaseCheck() {}

    @Nonnull
    public CheckDesc getCheckDesc() {
        return checkDesc;
    }

    public void setCheckDesc(@Nonnull CheckDesc checkDesc) {
        this.checkDesc = checkDesc;
    }

    @Nonnull
    public byte[] getCheckSignature() {
        return checkSignature;
    }

    public void setCheckSignature(@Nonnull byte[] checkSignature) {
        this.checkSignature = checkSignature;
    }
}
