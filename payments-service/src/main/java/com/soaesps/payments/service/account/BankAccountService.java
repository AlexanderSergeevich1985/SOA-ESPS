package com.soaesps.payments.service.account;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.soaesps.core.Utils.CryptoHelper;
import com.soaesps.core.service.archive.ArchiveServiceI;
import com.soaesps.payments.domain.transactions.AccountHistory;
import com.soaesps.payments.domain.transactions.BankAccount;
import com.soaesps.payments.domain.transactions.ServerBADesc;
import com.soaesps.payments.repository.ServerBankAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Nonnull;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * Service layer for managing bank accounts, including registration, modification, and archiving.
 */
@Service
@Transactional // Ensures all DB modifications within methods are atomic
public class BankAccountService implements BankAccountServiceI {

    public static final String ACCOUNT_ARCHIVE_PATH = "";

    private final ObjectMapper mapper;
    private final ArchiveServiceI archiveService;
    private final ServerBankAccountRepository serverBankAccountRepository;

    // FIXED: Replaced @Autowired field injection with constructor injection (Spring Boot best practice).
    // Injected Spring-managed ObjectMapper instead of creating a new instance manually.
    public BankAccountService(ObjectMapper mapper,
                              ArchiveServiceI archiveService,
                              ServerBankAccountRepository serverBankAccountRepository) {
        this.mapper = mapper;
        this.archiveService = archiveService;
        this.serverBankAccountRepository = serverBankAccountRepository;
    }

    @Override
    public BankAccount registerAccount(@Nonnull final BankAccount account) {
        account.setCreationTime(ZonedDateTime.now());
        final ServerBADesc desc = account.getServerBADesc();
        final KeyPair keyPair = CryptoHelper.generate_RSA();
        desc.setPublicKey(keyPair.getPublic().getEncoded());
        desc.setPrivateKey(keyPair.getPrivate().getEncoded());
        desc.setUuid(UUID.randomUUID());

        return serverBankAccountRepository.save(account);
    }

    @Override
    public boolean modifyAccount(@Nonnull final BankAccount accountNew) {
        final BankAccount account = serverBankAccountRepository.getReferenceById(accountNew.getId());
        copyBankAccount(accountNew, account);
        serverBankAccountRepository.save(account);

        return true;
    }

    @Override
    public boolean deleteAccount(@Nonnull final Long accountId) {
        if (serverBankAccountRepository.existsById(accountId)) {
            serverBankAccountRepository.deleteById(accountId);
            return true;
        }
        return false;
    }

    @Override
    public boolean archiveAccount(@Nonnull final Long accountId) throws Exception {
        final BankAccount account = serverBankAccountRepository.getReferenceById(accountId);
        final String accountStr = mapper.writeValueAsString(account);

        if (account.getHistory() == null) {
            final String name = archiveService.generateName(account.getServerBADesc().getOwnerId().toString());
            final String archiveName = CryptoHelper.getObjectDigest(CryptoHelper.getUuuid().concat(".").concat(name));
            final String archivePath = ACCOUNT_ARCHIVE_PATH.concat(archiveName);

            final AccountHistory history = new AccountHistory();
            history.setAccountId(account.getId());
            history.setArchivePath(archivePath);
            account.setHistory(history);

            InputStream inputStream = new ByteArrayInputStream(accountStr.getBytes(StandardCharsets.UTF_8));
            archiveService.archiveOne(archiveName, inputStream);

            serverBankAccountRepository.save(account);
        } else {
            final AccountHistory history = account.getHistory();
            final String oldArchivePath = ACCOUNT_ARCHIVE_PATH.concat(history.getArchivePath());
            final String name = archiveService.generateName(account.getServerBADesc().getOwnerId().toString());
            final String newArchiveName = CryptoHelper.getObjectDigest(CryptoHelper.getUuuid().concat(".").concat(name));
            final String newArchivePath = ACCOUNT_ARCHIVE_PATH.concat(newArchiveName);
            ArchiveServiceI.mergeFileWithArchive(newArchivePath, accountStr, oldArchivePath);
            history.setArchivePath(newArchivePath);
            serverBankAccountRepository.save(account); // Ensure updated history path is persisted
        }

        return true;
    }

    protected void copyBankAccount(@Nonnull final BankAccount accountNew, @Nonnull final BankAccount account) {
        account.setModificationTime(ZonedDateTime.now());
        account.setIndentation(accountNew.getIndentation());
        account.getServerBADesc().setAccountBalance(accountNew.getServerBADesc().getAccountBalance());
        account.getServerBADesc().setSharedSecret(accountNew.getServerBADesc().getSharedSecret());
    }
}