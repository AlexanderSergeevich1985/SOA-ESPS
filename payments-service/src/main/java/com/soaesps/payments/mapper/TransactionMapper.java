package com.soaesps.payments.mapper;

import com.soaesps.payments.DataModels.Transactions.BaseTransaction;
import com.soaesps.payments.dto.TransactionDto;

import org.mapstruct.Mapper;

import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Base64;

@Mapper(componentModel = "spring")
public interface TransactionMapper {

    // Maps Record DTO to JPA Entity
    BaseTransaction toEntity(TransactionDto dto);

    // Maps JPA Entity to Record DTO
    TransactionDto toDto(BaseTransaction entity);

    // Custom converter: Base64 String -> byte[]
    default byte[] map(String source) {
        if (source == null) {
            return null;
        }
        return Base64.getDecoder().decode(source);
    }

    // Custom converter: byte[] -> Base64 String
    default String map(byte[] source) {
        if (source == null) {
            return null;
        }
        return Base64.getEncoder().encodeToString(source);
    }

    default Timestamp mapZonedDateTimeToTimestamp(ZonedDateTime value) {
        if (value == null) {
            return null;
        }
        return Timestamp.from(value.toInstant());
    }

    default ZonedDateTime mapTimestampToZonedDateTime(Timestamp value) {
        if (value == null) {
            return null;
        }
        return value.toInstant().atZone(ZoneId.systemDefault());
    }
}