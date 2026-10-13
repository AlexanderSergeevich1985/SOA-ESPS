package com.soaesps.payments.mapper;

import com.soaesps.payments.DataModels.Transactions.BaseClientBill;
import com.soaesps.payments.DataModels.Transactions.ClientBillDesc;
import com.soaesps.payments.dto.ClientBillDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import java.util.Base64;

@Mapper(componentModel = "spring", uses = {ClientBillDescMapper.class})
public interface ClientBillMapper {

    // Maps Main Record DTO to JPA Entity
    @Mapping(target = "id", source = "id")
    @Mapping(target = "clientBillDesc", source = "clientBillDesc")
    @Mapping(target = "billSignature", source = "billSignature")
    BaseClientBill toEntity(ClientBillDto dto);

    // Maps Main JPA Entity to Record DTO
    @Mapping(target = "id", source = "id")
    @Mapping(target = "creationTime", ignore = true) // Игнорируем, если в BaseClientBill нет этого поля, либо настройте источник
    @Mapping(target = "clientBillDesc", source = "clientBillDesc")
    @Mapping(target = "billSignature", source = "billSignature")
    ClientBillDto toDto(BaseClientBill entity);

    default byte[] map(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return Base64.getDecoder().decode(value);
    }

    default String map(byte[] value) {
        if (value == null || value.length == 0) {
            return null;
        }
        return Base64.getEncoder().encodeToString(value);
    }
}