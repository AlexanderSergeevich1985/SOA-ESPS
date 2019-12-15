package com.soaesps.payments.service.account;

import com.soaesps.core.Utils.CryptoHelper;
import com.soaesps.core.service.archive.ArchiveServiceI;
import com.soaesps.payments.domain.transactions.BankAccount;
import com.soaesps.payments.domain.transactions.ServerBADesc;
import com.soaesps.payments.repository.ServerBankAccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.security.KeyPair;
import java.time.ZonedDateTime;
import java.util.UUID;

@Service
public class BankAccountService implements BankAccountServiceI {
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

    protected void copyBankAccount(@Nonnull final BankAccount accountNew, @Nonnull final BankAccount account) {
        account.setModificationTime(ZonedDateTime.now());
        account.setIndentation(accountNew.getIndentation());
        account.getServerBADesc().setAccountBalance(accountNew.getServerBADesc().getAccountBalance());
        account.getServerBADesc().setSharedSecret(accountNew.getServerBADesc().getSharedSecret());
    }
}