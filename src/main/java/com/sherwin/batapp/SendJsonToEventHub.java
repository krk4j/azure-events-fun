<dependencies>
    <!-- OpenTelemetry API -->
    <dependency>
        <groupId>io.opentelemetry</groupId>
        <artifactId>opentelemetry-api</artifactId>
        <version>1.24.0</version>
    </dependency>

    <!-- OpenTelemetry SDK -->
    <dependency>
        <groupId>io.opentelemetry</groupId>
        <artifactId>opentelemetry-sdk</artifactId>
        <version>1.24.0</version>
    </dependency>

    <!-- OpenTelemetry OTLP Exporter (for Tempo or Prometheus) -->
    <dependency>
        <groupId>io.opentelemetry.exporter</groupId>
        <artifactId>opentelemetry-exporter-otlp</artifactId>
        <version>1.24.0</version>
    </dependency>

    <!-- Optionally: OpenTelemetry Context Propagation (for HTTP, etc.) -->
    <dependency>
        <groupId>io.opentelemetry</groupId>
        <artifactId>opentelemetry-context</artifactId>
        <version>1.24.0</version>
    </dependency>
</dependencies>










    import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.sdk.resources.Resource;

public class OpenTelemetryConfig {

    public static void initializeOpenTelemetry() {
        // Set up OTLP exporter to send data to Grafana Tempo
        OtlpGrpcSpanExporter otlpExporter = OtlpGrpcSpanExporter.builder()
            .setEndpoint("http://localhost:4317") // Replace with your Grafana Tempo endpoint
            .build();

        // Configure TracerProvider with OTLP exporter
        SdkTracerProvider tracerProvider = SdkTracerProvider.builder()
            .addSpanProcessor(BatchSpanProcessor.builder(otlpExporter).build())
            .setResource(Resource.create(io.opentelemetry.semconv.resource.attributes.ResourceAttributes.SERVICE_NAME, "my-service"))
            .build();

        // Set the GlobalOpenTelemetry instance
        OpenTelemetrySdk.builder()
            .setTracerProvider(tracerProvider)
            .buildAndRegisterGlobal();
    }

    public static Tracer getTracer() {
        return GlobalOpenTelemetry.getTracer("instrumentation-library-name", "1.0.0");
    }
}



import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;

public class MyApp {
    private static final Tracer tracer = OpenTelemetryConfig.getTracer();

    public void processOrder() {
        // Start a new span
        Span span = tracer.spanBuilder("processOrder").startSpan();
        try {
            // Your business logic here
            performDatabaseOperation();
        } finally {
            // End the span when the operation is finished
            span.end();
        }
    }

    private void performDatabaseOperation() {
        Span span = tracer.spanBuilder("performDatabaseOperation").startSpan();
        try {
            // Simulate a database call
            Thread.sleep(100);
        } catch (InterruptedException e) {
            span.recordException(e);
        } finally {
            span.end();
        }
    }
}
=====================================================================================



    <dependencies>
    <!-- SLF4J API for logging abstraction -->
    <dependency>
        <groupId>org.slf4j</groupId>
        <artifactId>slf4j-api</artifactId>
        <version>1.7.32</version>
    </dependency>

    <!-- Logback for logging implementation -->
    <dependency>
        <groupId>ch.qos.logback</groupId>
        <artifactId>logback-classic</artifactId>
        <version>1.2.10</version>
    </dependency>

    <!-- OpenTelemetry Logging Library -->
    <dependency>
        <groupId>io.opentelemetry.instrumentation</groupId>
        <artifactId>opentelemetry-logback-appender</artifactId>
        <version>1.24.0</version>
    </dependency>

    <!-- OpenTelemetry SDK -->
    <dependency>
        <groupId>io.opentelemetry</groupId>
        <artifactId>opentelemetry-sdk</artifactId>
        <version>1.24.0</version>
    </dependency>

    <!-- OpenTelemetry Exporter for OTLP (for Grafana Loki or Tempo) -->
    <dependency>
        <groupId>io.opentelemetry.exporter</groupId>
        <artifactId>opentelemetry-exporter-otlp-logs</artifactId>
        <version>1.24.0</version>
    </dependency>
</dependencies>


src/main/resources/logback.xml 
    <configuration>

    <!-- Console Appender for local logging -->
    <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>

    <!-- OpenTelemetry Logback Appender for OTLP Exporter -->
    <appender name="OTEL" class="io.opentelemetry.instrumentation.logback.appender.OpenTelemetryAppender">
        <otelExporter>
            <otelOtlpExporter endpoint="http://localhost:4317" /> <!-- Grafana Loki/Tempo OTLP endpoint -->
        </otelExporter>
    </appender>

    <!-- Log everything at INFO level and above -->
    <root level="INFO">
        <appender-ref ref="STDOUT"/>
        <appender-ref ref="OTEL"/> <!-- Attach OTEL appender to root logger -->
    </root>

</configuration>

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.sdk.logs.SdkLoggerProvider;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.exporter.otlp.logs.OtlpGrpcLogExporter;
import io.opentelemetry.sdk.logs.export.BatchLogProcessor;

public class OpenTelemetryLoggingConfig {

    public static void initializeOpenTelemetry() {
        // Set up OTLP Log Exporter
        OtlpGrpcLogExporter logExporter = OtlpGrpcLogExporter.builder()
            .setEndpoint("http://localhost:4317") // Grafana Loki or Tempo endpoint
            .build();

        // Configure LoggerProvider with OTLP Exporter
        SdkLoggerProvider loggerProvider = SdkLoggerProvider.builder()
            .addLogProcessor(BatchLogProcessor.builder(logExporter).build())
            .build();

        // Set OpenTelemetry global instance
        OpenTelemetrySdk.builder()
            .setLoggerProvider(loggerProvider)
            .buildAndRegisterGlobal();
    }
}


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyApp {
    // Create an SLF4J logger
    private static final Logger logger = LoggerFactory.getLogger(MyApp.class);

    public static void main(String[] args) {
        // Initialize OpenTelemetry for logs
        OpenTelemetryLoggingConfig.initializeOpenTelemetry();

        // Log some events
        logger.info("Starting application...");
        try {
            performTask();
        } catch (Exception e) {
            logger.error("Error occurred while performing task", e);
        }

        logger.info("Application finished.");
    }

    private static void performTask() throws Exception {
        logger.debug("Performing task...");
        if (Math.random() > 0.5) {
            throw new Exception("Simulated task failure.");
        }
        logger.info("Task completed successfully.");
    }
}
    
