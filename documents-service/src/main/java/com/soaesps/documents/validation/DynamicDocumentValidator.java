package com.soaesps.documents.validation;

import com.soaesps.documents.domain.BaseDocument;
import com.soaesps.documents.service.ValidationSchemaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Enterprise polymorphic dynamic validator engine.
 * Decouples types verification from dynamic database constraint lists execution.
 */
@Component
public class DynamicDocumentValidator {

    private static final Logger log = LoggerFactory.getLogger(DynamicDocumentValidator.class);

    private final ValidationSchemaService schemaService;

    public DynamicDocumentValidator(ValidationSchemaService schemaService) {
        this.schemaService = schemaService;
    }

    public Mono<Boolean> validate(String documentType, BaseDocument document) {
        log.debug("[Polymorphic-Validator] Executing data-driven matrix for type: {}", documentType);

        return schemaService.getSchema(documentType.toUpperCase())
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Validation failed: No active schema rules registered for document type: " + documentType)))
                .map(schema -> {
                    Map<String, Object> actualProps = document.getProperties();

                    for (Map.Entry<String, ValidationSchema.PropertyDefinition> entry : schema.getPropertiesRules().entrySet()) {
                        String key = entry.getKey();
                        ValidationSchema.PropertyDefinition propDef = entry.getValue();
                        Object rawValue = actualProps.get(key);

                        // Validate required constraint rules boundaries
                        boolean isBlankString = (rawValue instanceof String str && str.isBlank());
                        if (propDef.required() && (rawValue == null || isBlankString)) {
                            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                    String.format("Validation Error: Field '%s' is strictly required for type '%s'", key, documentType));
                        }

                        if (rawValue == null || isBlankString) {
                            continue;
                        }

                        // Validate Type Uniformity constraints
                        validateTypeMatching(key, rawValue, propDef.type());

                        // Process the entire dynamic constraints list sequentially
                        for (ValidationSchema.Constraint constraint : propDef.constraints()) {
                            executeConstraintCheck(key, rawValue, constraint);
                        }
                    }
                    return Boolean.TRUE;
                });
    }

    private void validateTypeMatching(String key, Object value, String expectedType) {
        switch (expectedType.toUpperCase()) {
            case "STRING" -> {
                // If it's not a primitive string but some complex layout object, reject it
                if (value instanceof Collection || value instanceof Map) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("Type Mismatch: Field '%s' must be a String", key));
                }
            }
            case "NUMBER" -> {
                if (!(value instanceof Number)) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("Type Mismatch: Field '%s' must be a valid numeric token", key));
                }
            }
            case "BOOLEAN" -> {
                if (!(value instanceof Boolean)) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("Type Mismatch: Field '%s' must be a clear Boolean flag", key));
                }
            }
            case "ARRAY" -> {
                if (!(value instanceof Collection)) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("Type Mismatch: Field '%s' must be an iterable JSON Array", key));
                }
            }
        }
    }

    private void executeConstraintCheck(String key, Object value, ValidationSchema.Constraint constraint) {
        // Java 17+ pattern matching clean conditional layout routes
        if (constraint instanceof ValidationSchema.RegexConstraint(String pattern)) {
            String str = value.toString();
            if (!Pattern.matches(pattern, str)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("Constraint Violation: Field '%s' value violates format regex mask", key));
            }
        }
        else if (constraint instanceof ValidationSchema.LengthConstraint(Integer min, Integer max)) {
            int length = (value instanceof Collection<?> col) ? col.size() : value.toString().length();
            if (min != null && length < min) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("Constraint Violation: Field '%s' size/length %d must be >= %d", key, length, min));
            }
            if (max != null && length > max) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("Constraint Violation: Field '%s' size/length %d must be <= %d", key, length, max));
            }
        }
        else if (constraint instanceof ValidationSchema.NumericRangeConstraint(Double min, Double max)) {
            BigDecimal targetNumber = convertToBigDecimal(value);
            if (min != null) {
                BigDecimal minBound = BigDecimal.valueOf(min);
                if (targetNumber.compareTo(minBound) < 0) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("Constraint Violation: Numeric field '%s' value %s must be >= %s", key, targetNumber, min));
                }
            }
            if (max != null) {
                BigDecimal maxBound = BigDecimal.valueOf(max);
                if (targetNumber.compareTo(maxBound) > 0) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("Constraint Violation: Numeric field '%s' value %s must be <= %s", key, targetNumber, max));
                }
            }
        }
    }

    private BigDecimal convertToBigDecimal(Object value) {
        if (value instanceof BigDecimal bd) return bd;
        if (value instanceof Long || value instanceof Integer) {
            return BigDecimal.valueOf(((Number) value).longValue());
        }
        return new BigDecimal(value.toString());
    }
}