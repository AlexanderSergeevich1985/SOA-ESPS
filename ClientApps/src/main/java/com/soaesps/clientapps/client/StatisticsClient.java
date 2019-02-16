package com.soaesps.clientapps.client;

import com.soaesps.clientapps.domain.Statistic;
import com.soaesps.core.DataModels.device.DeviceInfo;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

import java.util.List;

public interface StatisticsClient {
    @GET("devices")
    Call<List<DeviceInfo>> getDevices();

    @GET("devices")
    Call<Statistic> getStatisticById(@Query("id") Integer id);
}