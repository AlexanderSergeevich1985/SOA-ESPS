package com.soaesps.documents.domain;

import org.springframework.data.mongodb.core.mapping.Field;
import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.Objects;

/**
 * Embedded model representing an approval or signing stamp of a document party.
 */
public class DocumentSignature implements Serializable {

    @Field("auth_id")
    private long authId;

    @Field("is_signed")
    private boolean signed;

    @Field("signature_date")
    private ZonedDateTime signatureDate;

    @Field("signed_hash")
    private String signedHash; // Captures document hash at the exact moment of signing

    public DocumentSignature() {}

    public DocumentSignature(long authId) {
        this.authId = authId;
        this.signed = false;
    }

    /**
     * Copy constructor for deep copying.
     */
    public DocumentSignature(DocumentSignature other) {
        if (other == null) return;
        this.authId = other.authId;
        this.signed = other.signed;
        this.signatureDate = other.signatureDate;
        this.signedHash = other.signedHash;
    }

    // --- Getters and Setters ---
    public long getAuthId() { return authId; }
    public void setAuthId(long authId) { this.authId = authId; }

    public boolean isSigned() { return signed; }
    public void setSigned(boolean signed) { this.signed = signed; }

    public ZonedDateTime getSignatureDate() { return signatureDate; }
    public void setSignatureDate(ZonedDateTime signatureDate) { this.signatureDate = signatureDate; }

    public String getSignedHash() { return signedHash; }
    public void setSignedHash(String signedHash) { this.signedHash = signedHash; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DocumentSignature other = (DocumentSignature) o;
        return authId == other.authId && signed == other.signed &&
                Objects.equals(signatureDate, other.signatureDate) &&
                Objects.equals(signedHash, other.signedHash);
    }

    @Override
    public int hashCode() {
        return Objects.hash(authId, signed, signatureDate, signedHash);
    }
}