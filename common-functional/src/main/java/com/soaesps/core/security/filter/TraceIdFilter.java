package com.soaesps.core.security.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.util.UUID;

@Component
public class TraceIdFilter implements Filter {

    // Key name used to reference the variable in log patterns
    private static final String TRACE_ID_KEY = "traceId";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;

        // Try to extract traceId from headers (useful for distributed tracing between microservices)
        String traceId = httpRequest.getHeader("X-Trace-Id");

        // Generate a new unique ID if the header is missing
        if (traceId == null || traceId.isEmpty()) {
            traceId = UUID.randomUUID().toString().substring(0, 8); // Keep the first 8 characters for readability
        }

        try {
            // Put the Trace ID into the current thread context (uses ThreadLocal under the hood)
            MDC.put(TRACE_ID_KEY, traceId);

            // Forward the request down the filter and controller chain
            chain.doFilter(request, response);

        } finally {
            // Clear the context securely when the thread returns to the Thread Pool
            // This prevents data pollution and memory leaks
            MDC.clear();
        }
    }
}