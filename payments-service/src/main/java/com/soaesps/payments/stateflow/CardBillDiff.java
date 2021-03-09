package com.soaesps.payments.stateflow;

import com.soaesps.payments.DataModels.Transactions.BaseServerBill;
import com.soaesps.payments.DataModels.Transactions.ServerBillDesc;

import java.math.BigDecimal;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

public class CardBillDiff {
    private final BaseServerBill serverBill;

    static public Map<String, AtomicReferenceFieldUpdater> updaters = new IdentityHashMap<>();

    private Map<String, Object> values = new IdentityHashMap<>();

    private BigDecimal accountBalanceDiff;

    public CardBillDiff(BaseServerBill serverBill) {
        this.serverBill = serverBill;
    }

    public <T> T getNewValue(String key, Class<T> tClass) {
        Object value = values.get(key);

        return tClass.isInstance(value) ? tClass.cast(values.get(key)) : null;
    }

    public void setNewValue(String key, Object value) {
        values.put(key, value);
    }

    public BigDecimal getAccountBalanceDiff() {
        return accountBalanceDiff;
    }

    public void setAccountBalanceDiff(BigDecimal accountBalanceDiff) {
        this.accountBalanceDiff = accountBalanceDiff;
    }

    public <T> void mergeUpdatedValue(String key, T value, Class<T> tClass) {
        AtomicReferenceFieldUpdater<BaseServerBill, T> updater = getUpdater(key, tClass);
        updater.set(serverBill, value);
    }

    public <T> AtomicReferenceFieldUpdater<BaseServerBill, T>  getUpdater(String key, Class<T> tClass) {
        AtomicReferenceFieldUpdater<BaseServerBill, T> updater = updaters.get(key);
        if (updater == null) {
            updater = AtomicReferenceFieldUpdater.newUpdater(BaseServerBill.class, tClass, key);
            updaters.put(key, updater);
        }

        return updater;
    }

    public void mergeAccountBalance() {
        BigDecimal result = getServerBillDesc().getAccountBalance().add(accountBalanceDiff);
        getServerBillDesc().setAccountBalance(result);
    }

    private ServerBillDesc getServerBillDesc() {
        return this.serverBill.getServerBillDesc();
    }
}