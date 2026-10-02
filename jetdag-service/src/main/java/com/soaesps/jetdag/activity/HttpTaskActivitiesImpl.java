package com.soaesps.jetdag.activity;

import io.temporal.activity.Activity;
import io.temporal.activity.ActivityExecutionContext;
import io.temporal.client.ActivityCompletionClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@RequiredArgsConstructor
@Slf4j
public class HttpTaskActivitiesImpl implements HttpTaskActivities {

    private final WebClient webClient;
    private final ActivityCompletionClient completionClient; // Temporal client to complete tasks asynchronously

    @Override
    public void executeHttpCall(String url, String method, String payloadJson) {
        // 1. Get the current execution context of this specific activity
        ActivityExecutionContext context = Activity.getExecutionContext();

        // 2. Fetch the unique binary token for this execution instance
        byte[] taskToken = context.getTaskToken();

        log.info("Reactive HTTP triggering -> [{}] {}. Detaching Temporal thread.", method, url);

        // 3. Tell Temporal: "Do not wait for this method to return. I will notify you via token later."
        context.doNotCompleteOnReturn();

        // 4. Fire completely non-blocking reactive HTTP request via WebClient
        webClient.method(HttpMethod.valueOf(method.toUpperCase()))
                .uri(url)
                .bodyValue(payloadJson != null ? payloadJson : "{}")
                .retrieve()
                .bodyToMono(String.class)
                // Triggered asynchronously when target microservice responds 200 OK
                .doOnSuccess(responseBody -> {
                    log.info("Target microservice responded 200 OK. Completing Temporal task.");
                    // Notify Temporal that this dynamic node in the DAG succeeded
                    completionClient.complete(taskToken, null);
                })
                // Triggered asynchronously if network fails, or 4xx/5xx error occurs
                .doOnError(error -> {
                    log.error("Target microservice returned an error: {}", error.getMessage());
                    // Notify Temporal about the failure to trigger retries or a fallback branch
                    completionClient.completeExceptionally(taskToken, new RuntimeException(error));
                })
                // Subscribe to start the reactive stream execution pipelines without blocking the main worker
                .subscribe();
    }
}