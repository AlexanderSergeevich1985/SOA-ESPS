package com.soaesps.documents.validation;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Document(collection = "validation_schemas")
public class ValidationSchema implements Serializable {
    @Id
    private String id;

    @Indexed(unique = true)
    @Field("document_type")
    private String documentType; // e.g., "PASSPORT", "FINANCIAL_DEAL"

    @Field("properties_rules")
    private Map<String, PropertyDefinition> propertiesRules = new HashMap<>();

    public ValidationSchema() {}

    // =========================================================================
    // CORE BOUNDARY TYPE DEFINITIONS
    // =========================================================================

    public record PropertyDefinition(
            String name,             // Field name duplicated for JSON flat visibility (e.g., "amount")
            String type,             // "STRING", "NUMBER", "BOOLEAN", "ARRAY"
            boolean required,
            List<Constraint> constraints // Extensible list of active validation rules
    ) implements Serializable {
        public PropertyDefinition {
            if (constraints == null) constraints = new ArrayList<>();
        }
    }

    // =========================================================================
    // POLYMORPHIC CONSTRAINTS HIERARCHY
    // =========================================================================

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "ruleType")
    @JsonSubTypes({
            @JsonSubTypes.Type(value = RegexConstraint.class, name = "REGEX"),
            @JsonSubTypes.Type(value = LengthConstraint.class, name = "LENGTH"),
            @JsonSubTypes.Type(value = NumericRangeConstraint.class, name = "NUMERIC_RANGE")
    })
    public interface Constraint extends Serializable {
        String ruleType(); // Identifies the constraint logic segment (REGEX, LENGTH, etc.)
    }

    public record RegexConstraint(
            String pattern
    ) implements Constraint {
        @Override public String ruleType() { return "REGEX"; }
    }

    public record LengthConstraint(
            Integer min,
            Integer max
    ) implements Constraint {
        @Override public String ruleType() { return "LENGTH"; }
    }

    public record NumericRangeConstraint(
            Double min,
            Double max
    ) implements Constraint {
        @Override public String ruleType() { return "NUMERIC_RANGE"; }
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getDocumentType() { return documentType; }
    public void setDocumentType(String documentType) { this.documentType = documentType; }
    public Map<String, PropertyDefinition> getPropertiesRules() { return propertiesRules; }
    public void setPropertiesRules(Map<String, PropertyDefinition> propertiesRules) { this.propertiesRules = propertiesRules; }
}