package com.soaesps.documentsservice.DataModels;

import org.hibernate.annotations.Type;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

import java.io.Serializable;
import java.time.ZonedDateTime;

@Document(collection = "document")
public class BaseDocument implements Serializable {
    @Id
    private long id;

    @Indexed(unique = true)
    @Field("domain")
    private String domain;

    @NotEmpty
    @Size(min = 5, max = 100)
    @Field("name")
    private String name;

    @Field("email")
    private String email;

    @Type(type="org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Field("create_date")
    private ZonedDateTime createDate;

    @Type(type="org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Field("date_of_publication")
    private ZonedDateTime dateOfPublication;

    @Field("create_auth_id")
    private long createAuthId;

    @Field("owner_auth_id")
    private long ownerAuthId;

    @Type(type="org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Field("end_date")
    private ZonedDateTime endDate;

    @Field("doc_Status")
    private int docStatus;

    @Type(type="org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Field("last_mofify_date")
    private ZonedDateTime lastModifyDate;

    @Field("last_auth_id")
    private long lastAuthId;

    @Field("last_operation_id")
    private long lastOperationId;

    public BaseDocument() {}

    public BaseDocument(final BaseDocument other) {
        this.id = other.id;
        this.email = other.email;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public ZonedDateTime getCreateDate() {
        return createDate;
    }

    public void setCreateDate(ZonedDateTime createDate) {
        this.createDate = createDate;
    }

    public ZonedDateTime getDateOfPublication() {
        return dateOfPublication;
    }

    public void setDateOfPublication(ZonedDateTime dateOfPublication) {
        this.dateOfPublication = dateOfPublication;
    }

    public long getCreateAuthId() {
        return createAuthId;
    }

    public void setCreateAuthId(long createAuthId) {
        this.createAuthId = createAuthId;
    }

    public long getOwnerAuthId() {
        return ownerAuthId;
    }

    public void setOwnerAuthId(long ownerAuthId) {
        this.ownerAuthId = ownerAuthId;
    }

    public ZonedDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(ZonedDateTime endDate) {
        this.endDate = endDate;
    }

    public int getDocStatus() {
        return docStatus;
    }

    public void setDocStatus(int docStatus) {
        this.docStatus = docStatus;
    }

    public ZonedDateTime getLastModifyDate() {
        return lastModifyDate;
    }

    public void setLastModifyDate(ZonedDateTime lastModifyDate) {
        this.lastModifyDate = lastModifyDate;
    }

    public long getLastAuthId() {
        return lastAuthId;
    }

    public void setLastAuthId(long lastAuthId) {
        this.lastAuthId = lastAuthId;
    }

    public long getLastOperationId() {
        return lastOperationId;
    }

    public void setLastOperationId(long lastOperationId) {
        this.lastOperationId = lastOperationId;
    }
}