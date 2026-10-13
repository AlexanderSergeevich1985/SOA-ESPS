package com.soaesps.payments.controller;

import com.soaesps.payments.DataModels.Transactions.BaseCheck;
import com.soaesps.payments.dto.ClientBillDto;
import com.soaesps.payments.dto.TransactionDto;
import com.soaesps.payments.service.PaymentsService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.util.Optional;

@RestController
@RequestMapping("/payments")
public class PaymentsController {
    @Autowired
    private PaymentsService paymentServices;

    @PostMapping(path = "/moneyTransfer")
    public ResponseEntity<?> transferMoney(@Valid @RequestBody TransactionDto transaction) {
        Optional<ClientBillDto> newBill = this.paymentServices.transferMoney(transaction);
        if(newBill.isPresent()) {
            return ResponseEntity.ok(newBill);
        }
        return ResponseEntity.badRequest().body("Invalid data request");
    }

    @PostMapping(path = "/cashCheck")
    public ResponseEntity<?> cashCheck(@Valid @RequestBody BaseCheck check) {
        Optional<ClientBillDto> newBill = this.paymentServices.cashCheck(check);
        if(newBill.isPresent()) {
            return ResponseEntity.ok(newBill);
        }
        return ResponseEntity.badRequest().body("Invalid data request");
    }

    @PostMapping(path = "/{billId}/refresh")
    public ResponseEntity<?> refreshBill(@PathVariable String billId) {
        Optional<ClientBillDto> newBill = this.paymentServices.refreshBill(billId);
        if(newBill.isPresent()) {
            return ResponseEntity.ok(newBill);
        }
        return ResponseEntity.badRequest().body("Invalid data request");
    }
}