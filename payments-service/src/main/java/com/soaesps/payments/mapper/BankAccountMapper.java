package com.soaesps.payments.mapper;

import com.soaesps.payments.domain.transactions.BankAccount;
import com.soaesps.payments.domain.transactions.AccountHistory;

import com.soaesps.payments.dto.AccountHistoryDto;
import com.soaesps.payments.dto.BankAccountDto;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", uses = {ServerBADescMapper.class})
public interface BankAccountMapper {

    // --- Entity to DTO Mappings ---

    @Mapping(target = "id", source = "id")
    @Mapping(target = "creationTime", source = "creationTime")
    @Mapping(target = "modificationTime", source = "modificationTime")
    @Mapping(target = "serverBADesc", source = "serverBADesc")
    @Mapping(target = "indentation", source = "indentation")
    @Mapping(target = "billSignature", source = "billSignature")
    @Mapping(target = "history", source = "history")
    BankAccountDto toDto(BankAccount entity);

    @Mapping(target = "accountId", source = "accountId")
    @Mapping(target = "archivePath", source = "archivePath")
    @Mapping(target = "password", source = "password")
    AccountHistoryDto toHistoryDto(AccountHistory entity);


    // --- DTO to Entity Mappings ---

    @Mapping(target = "id", source = "id")
    @Mapping(target = "creationTime", source = "creationTime")
    @Mapping(target = "modificationTime", source = "modificationTime")
    @Mapping(target = "serverBADesc", source = "serverBADesc")
    @Mapping(target = "indentation", source = "indentation")
    @Mapping(target = "billSignature", source = "billSignature")
    @Mapping(target = "history", source = "history")
    BankAccount toEntity(BankAccountDto dto);

    @Mapping(target = "accountId", source = "accountId")
    @Mapping(target = "archivePath", source = "archivePath")
    @Mapping(target = "password", source = "password")
    @Mapping(target = "bankAccount", ignore = true) // Handled in @AfterMapping to prevent circular references
    AccountHistory toHistoryEntity(AccountHistoryDto dto);


    // --- Bidirectional Relationship Handling ---

    /**
     * Establishes the backward relationship link between BankAccount and AccountHistory after entity mapping.
     */
    @AfterMapping
    default void linkHistory(@MappingTarget BankAccount bankAccount) {
        if (bankAccount.getHistory() != null) {
            bankAccount.getHistory().setBankAccount(bankAccount);
        }
    }
}
