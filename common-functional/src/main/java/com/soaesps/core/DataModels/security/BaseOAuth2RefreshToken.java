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
package com.soaesps.core.DataModels.security;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.soaesps.core.DataModels.BaseOnlyIdEntity;
import com.soaesps.core.Utils.DateTimeHelper;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonValue;
import org.springframework.security.oauth2.common.OAuth2RefreshToken;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.*;
import java.io.Serializable;
import java.time.Duration;
import java.util.Date;

@Entity
@Table(name = "ISSUED_REFRESH_TOKENS")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class BaseOAuth2RefreshToken extends BaseOnlyIdEntity implements Serializable, OAuth2RefreshToken {
    @Transient
    private static final long DEFAULT_EXP_PERIOD_SECONDS = 1 * 60 * 60;

    @Transient
    private int counter; // this need for checking number of requests during current session

    @Transient
    private String question; // this generated by IAutoEncoder

    @Transient
    private String answear; // this generated by IAutoEncoder

    @Column(name = "acc_token_value", length = 500, nullable = false)
    private String value;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "ref_token_exp_date", nullable = false)
    private Date expiration;

    protected BaseOAuth2RefreshToken() {}

    @JsonCreator
    public BaseOAuth2RefreshToken(final String value, final long interval) {
        this.value = value;
        this.expiration = Date.from(DateTimeHelper.calcExpirationDate(Duration.ofSeconds(interval)));
    }

    @Override
    @JsonValue
    @Nonnull
    public String getValue() {
        return value;
    }

    public void setValue(@Nonnull final String value) {
        this.value = value;
    }

    @JsonValue
    @Nonnull
    public Date getExpiration() {
        return this.expiration;
    }

    public void setExpiration(@Nonnull final Date expiration) {
        this.expiration = expiration;
    }

    @JsonValue
    @Nullable
    public int getCounter() {
        return counter;
    }

    public void setCounter(@Nullable final int counter) {
        this.counter = counter;
    }

    public void incrementCounter() {
        ++counter;
    }

    @JsonValue
    @Nullable
    public String getQuestion() {
        return this.question;
    }

    public void setQuestion(@Nullable final String question) {
        this.question = question;
    }

    @JsonValue
    @Nullable
    public String getAnswear() {
        return answear;
    }

    public void setAnswear(@Nullable final String answear) {
        this.answear = answear;
    }
}