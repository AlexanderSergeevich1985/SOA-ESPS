package com.soaesps.clientapps.client;

import com.soaesps.payments.DataModels.Transactions.BaseCheck;
import com.soaesps.payments.DataModels.Transactions.BaseTransaction;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface PaymentsClient {
    @PUT("payments/moneyTransfer")
    Call<?> transferMoney(@Body BaseTransaction transaction);

    @POST("payments/cashCheck")
    Call<?> cashCheck(@Body BaseCheck check);

    @POST("payments/{billId}/refresh")
    Call<?> refreshBill(@Path("billId") String billId);
}