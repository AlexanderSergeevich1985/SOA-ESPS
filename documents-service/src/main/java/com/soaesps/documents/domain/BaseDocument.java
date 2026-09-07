package com.soaesps.documents.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Base MongoDB document model.
 */
@Document(collection = "document")
public class BaseDocument implements Serializable {
    @Id
    private String id;

    @Indexed(unique = true)
    @Field("domain")
    private String domain;

    @NotEmpty
    @Size(min = 5, max = 100)
    @Field("name")
    private String name;

    @Field("email")
    private String email;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @Field("create_date")
    private ZonedDateTime createDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @Field("date_of_publication")
    private ZonedDateTime dateOfPublication;

    @Field("create_auth_id")
    private long createAuthId;

    @Field("owner_auth_id")
    private long ownerAuthId;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @Field("end_date")
    private ZonedDateTime endDate;

    @Field("doc_Status")
    private int docStatus;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @Field("last_mofify_date") // NOTE: Typo "mofify" preserved to match existing DB field name
    private ZonedDateTime lastModifyDate;

    @Field("last_auth_id")
    private long lastAuthId;

    @Field("last_operation_id")
    private long lastOperationId;

    @Field("hash")
    private String hash;

    @Indexed
    @Field("package_id")
    private String packageId;

    @Indexed
    @Field("parent_id")
    private String parentId;

    // Добавлено: Динамические свойства документа для MongoDB
    @Field("properties")
    private Map<String, Object> properties = new LinkedHashMap<>();

    @Field("signatures")
    private List<DocumentSignature> signatures = new ArrayList<>();

    public BaseDocument() {}

    public BaseDocument(final BaseDocument other) {
        if (other == null) {
            return;
        }
        this.id = other.id;
        this.domain = other.domain;
        this.name = other.name;
        this.email = other.email;
        this.createDate = other.createDate;
        this.dateOfPublication = other.dateOfPublication;
        this.createAuthId = other.createAuthId;
        this.ownerAuthId = other.ownerAuthId;
        this.endDate = other.endDate;
        this.docStatus = other.docStatus;
        this.lastModifyDate = other.lastModifyDate;
        this.lastAuthId = other.lastAuthId;
        this.lastOperationId = other.lastOperationId;
        this.hash = other.hash;
        this.packageId = other.packageId;
        this.parentId = other.parentId;

        if (other.properties != null) {
            this.properties = new LinkedHashMap<>(other.properties);
        } else {
            this.properties = new LinkedHashMap<>();
        }

        // Deep copy of approval signatures list
        if (other.signatures != null) {
            this.signatures = other.signatures.stream()
                    .map(DocumentSignature::new)
                    .collect(Collectors.toList());
        } else {
            this.signatures = new ArrayList<>();
        }
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getDomain() { return domain; }
    public void setDomain(String domain) { this.domain = domain; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public ZonedDateTime getCreateDate() { return createDate; }
    public void setCreateDate(ZonedDateTime createDate) { this.createDate = createDate; }

    public ZonedDateTime getDateOfPublication() { return dateOfPublication; }
    public void setDateOfPublication(ZonedDateTime dateOfPublication) { this.dateOfPublication = dateOfPublication; }

    public long getCreateAuthId() { return createAuthId; }
    public void setCreateAuthId(long createAuthId) { this.createAuthId = createAuthId; }

    public long getOwnerAuthId() { return ownerAuthId; }
    public void setOwnerAuthId(long ownerAuthId) { this.ownerAuthId = ownerAuthId; }

    public ZonedDateTime getEndDate() { return endDate; }
    public void setEndDate(ZonedDateTime endDate) { this.endDate = endDate; }

    public int getDocStatus() { return docStatus; }
    public void setDocStatus(int docStatus) { this.docStatus = docStatus; }

    public ZonedDateTime getLastModifyDate() { return lastModifyDate; }
    public void setLastModifyDate(ZonedDateTime lastModifyDate) { this.lastModifyDate = lastModifyDate; }

    public long getLastAuthId() { return lastAuthId; }
    public void setLastAuthId(long lastAuthId) { this.lastAuthId = lastAuthId; }

    public long getLastOperationId() { return lastOperationId; }
    public void setLastOperationId(long lastOperationId) { this.lastOperationId = lastOperationId; }

    public String getHash() { return hash; }
    public void setHash(String hash) { this.hash = hash; }

    public String getPackageId() { return packageId; }
    public void setPackageId(String packageId) { this.packageId = packageId; }
    public String getParentId() { return parentId; }
    public void setParentId(String parentId) { this.parentId = parentId; }

    public Map<String, Object> getProperties() { return properties; }
    public void setProperties(Map<String, Object> properties) { this.properties = properties; }

    public List<DocumentSignature> getSignatures() { return signatures; }
    public void setSignatures(List<DocumentSignature> signatures) { this.signatures = signatures; }

    // =========================================================================
    // STANDARD EQUALS & HASHCODE (Business Identity Contract)
    // =========================================================================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BaseDocument other = (BaseDocument) o;

        if (id != null && other.id != null) {
            return id.equals(other.id);
        }

        return Objects.equals(domain, other.domain);
    }

    @Override
    public int hashCode() {
        if (id != null) {
            return id.hashCode();
        }
        return domain != null ? domain.hashCode() : 0;
    }
}
