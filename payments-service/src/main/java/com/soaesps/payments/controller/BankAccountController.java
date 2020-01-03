package com.soaesps.payments.controller;

import com.soaesps.payments.domain.transactions.BankAccount;
import com.soaesps.payments.service.account.BankAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/bank_account")
public class BankAccountController {
    @Autowired
    private BankAccountService bankAccountService;

    @PostMapping(path = "/register")
    public ResponseEntity<?> registerBankAccount(@Valid @RequestBody BankAccount account) {
        final BankAccount newAccount = this.bankAccountService.registerAccount(account);
        if (newAccount != null) {
            return ResponseEntity.ok(newAccount);
        }

        return ResponseEntity.badRequest().body("Invalid data request");
    }

    @PutMapping(path = "/modify")
    public ResponseEntity<?> modifyBankAccount(@Valid @RequestBody BankAccount account) {
        final BankAccount modifyAccount = this.bankAccountService.modifyAccount(account);
        if (modifyAccount != null) {
            return ResponseEntity.ok(modifyAccount);
        }

        return ResponseEntity.badRequest().body("Invalid data request");
    }

    @DeleteMapping(path = "/delete")
    public ResponseEntity<?> deleteBankAccount(@Valid Integer accountId) {
        if (this.bankAccountService.deleteAccount(accountId)) {
            return ResponseEntity.status(HttpStatus.OK).build();
        }

        return ResponseEntity.badRequest().body("Invalid data request");
    }

    @DeleteMapping(path = "/archive")
    public ResponseEntity<?> archiveBankAccount(@Valid Integer accountId) throws Exception {
        if (this.bankAccountService.archiveAccount(accountId)) {
            return ResponseEntity.status(HttpStatus.OK).build();
        }

        return ResponseEntity.badRequest().body("Invalid data request");
    }
}