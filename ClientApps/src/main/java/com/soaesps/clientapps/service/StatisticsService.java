package com.soaesps.clientapps.service;

import com.soaesps.clientapps.client.StatisticsClient;
import com.soaesps.core.DataModels.device.DeviceInfo;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.security.Principal;
import java.util.List;

public class StatisticsService {
    public static String baseUrl = "https://my_api.gateway.com";

    private StatisticsClient statClient;

    public StatisticsService() {
        this(null);
    }

    public StatisticsService(final String baseUrl_) {
        if (baseUrl_ != null && baseUrl_.trim().length() > 0) baseUrl = baseUrl_;
        final Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(JacksonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();

        statClient = retrofit.create(StatisticsClient.class);
    }

    List<String> getDevicesNameBySoftType(final Principal principal, final String devSoftType) {
        return this.statClient.getDevices(principal)
                .flatMapIterable(x -> x)
                .filter(x -> x.getDeviceSoftModel().equals(devSoftType))
                .map(DeviceInfo::getName)
                .sorted((x1, x2) -> x1.compareTo(x2))
                .distinct()
                .toList()
                .toBlocking()
                .single();
    }

    List<DeviceInfo> getDevices(final Principal principal) {
        return this.statClient.getDevices(principal)
                .flatMapIterable(x -> x)
                .sorted((x1, x2) -> x1.getName().compareTo(x2.getName()))
                .distinct(DeviceInfo::getName)
                .toList()
                .toBlocking()
                .single();
    }
}