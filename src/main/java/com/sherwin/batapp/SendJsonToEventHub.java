import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MyCustomListConverter extends AbstractHttpMessageConverter<List<MyRequest>> {

    private final ObjectMapper objectMapper;

    public MyCustomListConverter() {
        super(MediaType.APPLICATION_JSON);
        this.objectMapper = new ObjectMapper();
    }

    @Override
    protected boolean supports(Class<?> clazz) {
        return List.class.isAssignableFrom(clazz);
    }

    @Override
    protected List<MyRequest> readInternal(Class<? extends List<MyRequest>> clazz, HttpInputMessage inputMessage) throws IOException {
        return objectMapper.readValue(inputMessage.getBody(), objectMapper.getTypeFactory().constructCollectionType(ArrayList.class, MyRequest.class));
    }

    @Override
    protected void writeInternal(List<MyRequest> myRequests, HttpOutputMessage outputMessage) throws IOException {
        objectMapper.writeValue(outputMessage.getBody(), myRequests);
    }
}






import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.add(new MyCustomListConverter());
    }
}
