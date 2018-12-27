package com.soaesps.payments.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/payments")
public class PaymentsController {
    @Autowired
    private PaymentsService paymentServices;

    @PostMapping(path = "/moneyTransfer")
    public ResponseEntity<?> transferMoney(@Valid @RequestBody BaseTransaction transaction) {
        BaseClientBill newBill = this.paymentServices.transferMoney(transaction);
        if(newBill != null) {
            return ResponseEntity.ok(newBill);
        }
        return ResponseEntity.badRequest().body("Invalid data request");
    }

    @PostMapping(path = "/cashCheck")
    public ResponseEntity<?> cashCheck(@Valid @RequestBody BaseCheck check) {
        BaseClientBill newBill = this.paymentServices.cashCheck(check);
        if(newBill != null) {
            return ResponseEntity.ok(newBill);
        }
        return ResponseEntity.badRequest().body("Invalid data request");
    }

    @PostMapping(path = "/{billId}/refresh")
    public ResponseEntity<?> refreshBill(@PathVariable String billId) {
        BaseClientBill newBill = this.paymentServices.refreshBill(billId);
        if(newBill != null) {
            return ResponseEntity.ok(newBill);
        }
        return ResponseEntity.badRequest().body("Invalid data request");
    }
}