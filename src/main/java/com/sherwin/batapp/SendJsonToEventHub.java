import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HttpClientConfig {

    @Bean
    public CloseableHttpClient httpClient() {
        return HttpClientBuilder.create()
                .setRetryHandler(new DefaultHttpRequestRetryHandler(3, true))
                .build();
    }
}




import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class MyService {

    @Autowired
    private CloseableHttpClient httpClient;

    public String makeRequest() throws IOException {
        HttpGet request = new HttpGet("http://example.com");
        try (CloseableHttpResponse response = httpClient.execute(request)) {
            return response.getEntity() != null ? response.getEntity().toString() : null;
        }
    }
}


import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.protocol.HttpContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.io.IOException;

@Configuration
public class HttpClientConfig {

    @Bean
    public CloseableHttpClient httpClient() {
        return HttpClientBuilder.create()
                .setRetryHandler(new CustomRetryHandler())
                .build();
    }
}

class CustomRetryHandler extends DefaultHttpRequestRetryHandler {

    public CustomRetryHandler() {
        super(3, true);  // Set the max retry count and requestSentRetryEnabled
    }

    @Override
    public boolean retryRequest(IOException exception, int executionCount, HttpContext context) {
        if (executionCount >= 3) {
            return false;  // Do not retry if over max retry count
        }
        if (exception instanceof org.apache.http.NoHttpResponseException) {
            return true;  // Retry if the server dropped connection on us
        }
        // Add more custom retry logic here if needed
        return super.retryRequest(exception, executionCount, context);
    }
}
