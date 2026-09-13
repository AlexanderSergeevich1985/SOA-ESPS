package com.nukefintech.uitoken.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;

@Document(collection = "ui_token_layouts")
// Strict compound index ensures unique design systems delivery combinations
@CompoundIndex(name = "layout_lookup_idx", def = "{'document_type': 1, 'view_mode': 1, 'density': 1}", unique = true)
public class UiTokenLayout implements Serializable {

    @Id
    private String id;

    @Field("document_type")
    private String documentType; // e.g., "COLLATERAL_PLEDGE", "TERM_SHEET"

    @Field("view_mode")
    private String viewMode;     // e.g., "DARK", "LIGHT", "EXPERT"

    private String density;      // e.g., "COMPACT", "COMFORTABLE"

    @Field("schema_version")
    private int schemaVersion;   // Forces frontend to match strict backend validation core constraints version

    @Field("tokens")
    private TokenRegistry tokens; // Inner atomics dictionary properties bag

    public UiTokenLayout() {}

    public static record TokenRegistry(
            String themeName,
            String primaryColor,
            String accentColor,
            String borderRadius,
            String spacingGap
    ) implements Serializable {}

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getDocumentType() { return documentType; }
    public void setDocumentType(String documentType) { this.documentType = documentType; }
    public String getViewMode() { return viewMode; }
    public void setViewMode(String viewMode) { this.viewMode = viewMode; }
    public String getDensity() { return density; }
    public void setDensity(String density) { this.density = density; }
    public int getSchemaVersion() { return schemaVersion; }
    public void setSchemaVersion(int schemaVersion) { this.schemaVersion = schemaVersion; }
    public TokenRegistry getTokens() { return tokens; }
    public void setTokens(TokenRegistry tokens) { this.tokens = tokens; }
}