package com.soaesps.payments.controller;

import com.soaesps.payments.domain.transactions.BankAccount;
import com.soaesps.payments.service.account.BankAccountService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * REST controller for managing bank accounts.
 */
@RestController
@RequestMapping("/bank_account")
public class BankAccountController {

    private final BankAccountService bankAccountService;

    public BankAccountController(BankAccountService bankAccountService) {
        this.bankAccountService = bankAccountService;
    }

    @PostMapping(path = "/register", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> registerBankAccount(@Valid @RequestBody BankAccount account) {
        final BankAccount newAccount = this.bankAccountService.registerAccount(account);
        if (newAccount != null) {
            return ResponseEntity.ok(newAccount);
        }
        return ResponseEntity.badRequest().body("Invalid data request");
    }

    @PutMapping(path = "/modify", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> modifyBankAccount(@Valid @RequestBody BankAccount account) {
        final boolean isModified = this.bankAccountService.modifyAccount(account);
        if (isModified) {
            // Returning 200 OK. If the client needs the updated entity, the service should be refactored to return it.
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.badRequest().body("Invalid data request or account not found");
    }

    @DeleteMapping(path = "/delete")
    public ResponseEntity<?> deleteBankAccount(@Valid @RequestParam("accountId") Long accountId) {
        if (this.bankAccountService.deleteAccount(accountId)) {
            return ResponseEntity.status(HttpStatus.OK).build();
        }
        return ResponseEntity.badRequest().body("Invalid data request or account not found");
    }

    @PostMapping(path = "/archive")
    public ResponseEntity<?> archiveBankAccount(@Valid @RequestParam("accountId") Long accountId) throws Exception {
        if (this.bankAccountService.archiveAccount(accountId)) {
            return ResponseEntity.status(HttpStatus.OK).build();
        }
        return ResponseEntity.badRequest().body("Invalid data request or account not found");
    }
}