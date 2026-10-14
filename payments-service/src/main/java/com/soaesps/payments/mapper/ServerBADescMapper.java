package com.soaesps.payments.mapper;

import com.soaesps.payments.domain.transactions.ServerBADesc;
import com.soaesps.payments.dto.ServerBADescDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ServerBADescMapper {

    ServerBADescDto toDto(ServerBADesc entity);

    ServerBADesc toEntity(ServerBADescDto dto);
}