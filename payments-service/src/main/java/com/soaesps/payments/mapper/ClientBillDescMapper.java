package com.soaesps.payments.mapper;

import com.soaesps.payments.DataModels.Transactions.ClientBillDesc;
import com.soaesps.payments.dto.ClientBillDescDto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import java.util.Base64;

@Mapper(componentModel = "spring")
public interface ClientBillDescMapper {

    // Maps Record DTO to JPA Entity / Embedded Object
    @Mapping(source = "serverPublicKey", target = "serverPublicKey", qualifiedByName = "stringToBytes")
    ClientBillDesc toEntity(ClientBillDescDto dto);

    // Maps JPA Entity back to Record DTO
    @Mapping(source = "serverPublicKey", target = "serverPublicKey", qualifiedByName = "bytesToString")
    ClientBillDescDto toDto(ClientBillDesc entity);

    // Custom converter: Base64 String -> byte[]
    @Named("stringToBytes")
    default byte[] stringToBytes(String source) {
        if (source == null) {
            return null;
        }
        return Base64.getDecoder().decode(source);
    }

    // Custom converter: byte[] -> Base64 String
    @Named("bytesToString")
    default String bytesToString(byte[] source) {
        if (source == null) {
            return null;
        }
        return Base64.getEncoder().encodeToString(source);
    }
}