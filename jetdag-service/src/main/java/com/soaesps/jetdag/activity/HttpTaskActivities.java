package com.soaesps.jetdag.activity;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

@ActivityInterface
public interface HttpTaskActivities {

    /**
     * Executes a dynamic HTTP call to a target microservice endpoint defined in the DAG.
     */
    @ActivityMethod
    void executeHttpCall(String url, String method, String payloadJson);
}