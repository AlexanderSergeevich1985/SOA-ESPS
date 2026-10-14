package com.soaesps.payments.mapper;

import com.soaesps.payments.DataModels.Transactions.BaseCheck;
import com.soaesps.payments.DataModels.Transactions.CheckDesc;

import com.soaesps.payments.dto.CheckDescDto;
import com.soaesps.payments.dto.CheckDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Base64;

@Mapper(componentModel = "spring")
public interface CheckMapper {

    // --- Entity to DTO Mappings ---

    @Mapping(target = "id", source = "id")
    @Mapping(target = "checkDesc", source = "checkDesc")
    CheckDto toDto(BaseCheck entity);

    @Mapping(target = "signTime", source = "signTime", qualifiedByName = "timestampToInstant")
    CheckDescDto toDescDto(CheckDesc entity);


    // --- DTO to Entity Mappings ---

    @Mapping(target = "id", source = "id")
    @Mapping(target = "checkDesc", source = "checkDesc")
    BaseCheck toEntity(CheckDto dto);

    @Mapping(target = "signTime", source = "signTime", qualifiedByName = "instantToTimestamp")
    CheckDesc toDescEntity(CheckDescDto dto);


    // --- Type Conversion Utility Methods ---

    @Named("timestampToInstant")
    default Instant timestampToInstant(Timestamp timestamp) {
        if (timestamp == null) {
            return null;
        }
        return timestamp.toInstant();
    }

    @Named("instantToTimestamp")
    default Timestamp instantToTimestamp(Instant instant) {
        if (instant == null) {
            return null;
        }
        return Timestamp.from(instant);
    }

    // --- Base64 Helper Methods (Optional for JSON Strings) ---

    @Named("bytesToBase64")
    default String bytesToBase64(byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        return Base64.getEncoder().encodeToString(bytes);
    }

    @Named("base64ToBytes")
    default byte[] base64ToBytes(String base64) {
        if (base64 == null) {
            return null;
        }
        return Base64.getDecoder().decode(base64);
    }
}