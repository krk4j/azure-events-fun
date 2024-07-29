package com.example;

import com.microsoft.azure.functions.annotation.*;
import com.microsoft.azure.functions.*;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.vavr.control.Try;

import java.time.Duration;

public class Function {

    private static final CircuitBreaker circuitBreaker;

    static {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .failureRateThreshold(50)
                .waitDurationInOpenState(Duration.ofSeconds(60))
                .permittedNumberOfCallsInHalfOpenState(3)
                .slidingWindowSize(10)
                .build();

        CircuitBreakerRegistry registry = CircuitBreakerRegistry.of(config);
        circuitBreaker = registry.circuitBreaker("azureFunctionCircuitBreaker");
    }

    @FunctionName("HttpTrigger-Java")
    public HttpResponseMessage run(
        @HttpTrigger(name = "req", methods = {HttpMethod.GET, HttpMethod.POST}, authLevel = AuthorizationLevel.ANONYMOUS)
        HttpRequestMessage<Optional<String>> request,
        final ExecutionContext context) {

        context.getLogger().info("Java HTTP trigger processed a request.");

        Try<HttpResponseMessage> result = Try.of(circuitBreaker.decorateCheckedSupplier(() -> {
            // Your function logic here
            String query = request.getQueryParameters().get("name");
            String name = request.getBody().orElse(query);

            if (name == null) {
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body("Please pass a name on the query string or in the request body").build();
            } else {
                return request.createResponseBuilder(HttpStatus.OK).body("Hello, " + name).build();
            }
        }));

        if (result.isFailure()) {
            // Handle failure case
            return request.createResponseBuilder(HttpStatus.SERVICE_UNAVAILABLE).body("Service is unavailable. Please try again later.").build();
        }

        return result.get();
    }
}
