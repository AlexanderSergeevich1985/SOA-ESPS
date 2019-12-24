package com.soaesps.payments.service.account;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.soaesps.core.Utils.CryptoHelper;
import com.soaesps.core.service.archive.ArchiveServiceI;
import com.soaesps.payments.domain.transactions.AccountHistory;
import com.soaesps.payments.domain.transactions.BankAccount;
import com.soaesps.payments.domain.transactions.ServerBADesc;
import com.soaesps.payments.repository.ServerBankAccountRepository;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.security.KeyPair;
import java.time.ZonedDateTime;
import java.util.UUID;

@Service
public class BankAccountService implements BankAccountServiceI {
    static public final String ACCOUNT_ARCHIVE_PATH = "";

    private ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private ArchiveServiceI archiveService;

    @Autowired
    private ServerBankAccountRepository serverBankAccountRepository;

    public BankAccountService() {}

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
        final BankAccount account = serverBankAccountRepository.getOne(accountNew.getId());
        copyBankAccount(accountNew, account);
        serverBankAccountRepository.save(account);

        return false;
    }

    @Override
    public boolean deleteAccount(@Nonnull final Integer accountId) {
        if (serverBankAccountRepository.existsById(accountId)) {
            serverBankAccountRepository.deleteById(accountId);

            return true;
        }

        return false;
    }

    @Override
    public boolean archiveAccount(@Nonnull final Integer accountId) throws Exception {
        final BankAccount account = serverBankAccountRepository.getOne(accountId);
        if (account.getHistory() == null) {
            final String name = archiveService.generateName(account.getServerBADesc().getOwnerId().toString());
            final String archiveName = CryptoHelper.getObjectDigest(CryptoHelper.getUuuid().concat(".").concat(name));
            final String archivePath = ACCOUNT_ARCHIVE_PATH.concat(archiveName);
            final AccountHistory history = new AccountHistory();
            history.setAccountId(account.getId());
            history.setArchivePath(archivePath);
            final String accountStr = mapper.writeValueAsString(account);
            archiveService.archiveOne(archiveName, IOUtils.toInputStream(accountStr));
            serverBankAccountRepository.save(account);
        } else {
            final AccountHistory history = account.getHistory();
            final String archivePath = ACCOUNT_ARCHIVE_PATH.concat(history.getArchivePath());
            final String accountStr = mapper.writeValueAsString(account);\
            //archiveService.addToArchive(archivePath, IOUtils.toInputStream(accountStr), );
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