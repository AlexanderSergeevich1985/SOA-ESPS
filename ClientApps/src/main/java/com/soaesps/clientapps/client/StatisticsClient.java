package com.soaesps.clientapps.client;

import com.soaesps.clientapps.domain.Statistic;
import com.soaesps.core.DataModels.device.DeviceInfo;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Query;
import rx.Observable;

import java.security.Principal;
import java.util.List;

public interface StatisticsClient {
    @GET("devices")
    Observable<List<DeviceInfo>> getDevices(@Body Principal principal);

    @GET("devices")
    Observable<Statistic> getStatisticById(@Query("id") Integer id);
}