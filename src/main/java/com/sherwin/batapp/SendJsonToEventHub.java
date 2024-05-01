package com.sherwin.batapp;

import com.azure.messaging.eventhubs.*;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class SendJsonToEventHub {

   <dependency>
    <groupId>com.microsoft.azure</groupId>
    <artifactId>azure-eventhubs</artifactId>
    <version>5.3.0</version>
</dependency>
<dependency>
    <groupId>org.apache.avro</groupId>
    <artifactId>avro</artifactId>
    <version>1.10.2</version>
</dependency>
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
    <version>2.13.1</version>
</dependency>














            import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

    public class AvroJsonConverter {
        private static final ObjectMapper objectMapper = new ObjectMapper();

        public static byte[] avroToJson(Schema schema, GenericRecord record) throws IOException {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            JsonEncoder encoder = EncoderFactory.get().jsonEncoder(schema, outputStream);
            DatumWriter<GenericRecord> writer = new GenericDatumWriter<>(schema);
            writer.write(record, encoder);
            encoder.flush();
            return outputStream.toByteArray();
        }

        public static GenericRecord jsonToAvro(Schema schema, byte[] json) throws IOException {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(json);
            Decoder decoder = DecoderFactory.get().jsonDecoder(schema, inputStream);
            DatumReader<GenericRecord> reader = new GenericDatumReader<>(schema);
            return reader.read(null, decoder);
        }

        public static String toJson(Object object) throws IOException {
            return objectMapper.writeValueAsString(object);
        }

        public static <T> T fromJson(String json, Class<T> clazz) throws IOException {
            return objectMapper.readValue(json, clazz);
        }
    }
















    import com.microsoft.azure.eventhubs.*;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

    public class EventHubAvroJsonExample {

        private static final String EVENT_HUB_NAMESPACE = "<event_hub_namespace>";
        private static final String EVENT_HUB_NAME = "<event_hub_name>";
        private static final String SAS_KEY_NAME = "<sas_key_name>";
        private static final String SAS_KEY = "<sas_key>";
        private static final String CONNECTION_STRING_FORMAT = "Endpoint=sb://%s.servicebus.windows.net/;SharedAccessKeyName=%s;SharedAccessKey=%s;EntityPath=%s";

        private static final Schema AVRO_SCHEMA = new Schema.Parser().parse("{ \"type\":\"record\", \"name\":\"test\", \"fields\":[{\"name\":\"name\",\"type\":\"string\"}]}");

        public static void main(String[] args) throws Exception {
            String connStr = String.format(CONNECTION_STRING_FORMAT, EVENT_HUB_NAMESPACE, SAS_KEY_NAME, SAS_KEY, EVENT_HUB_NAME);

            EventHubClient eventHubClient = EventHubClient.createFromConnectionStringSync(connStr);
            EventHubRuntimeInformation eventHubInfo = eventHubClient.getRuntimeInformation().get();

            ExecutorService executorService = Executors.newSingleThreadExecutor();

            for (String partitionId : eventHubInfo.getPartitionIds()) {
                executorService.submit(() -> {
                    try {
                        PartitionReceiver receiver = eventHubClient.createReceiverSync("$Default", partitionId, EventHubClient.DEFAULT_CONSUMER_GROUP_NAME, ReceiverOptions.DEFAULT);

                        while (true) {
                            Iterable<EventData> messages = receiver.receiveSync(100);

                            for (EventData eventData : messages) {
                                byte[] avroMessage = eventData.getBytes();
                                GenericRecord record = AvroJsonConverter.jsonToAvro(AVRO_SCHEMA, avroMessage);
                                String json = AvroJsonConverter.toJson(record);
                                System.out.println("Received message as JSON: " + json);

                                // Example of converting JSON back to Avro
                                GenericRecord avroRecord = AvroJsonConverter.fromJson(json, GenericRecord.class);
                                byte[] avroBytes = AvroJsonConverter.avroToJson(AVRO_SCHEMA, avroRecord);
                                // Process avroBytes...
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }
        }
    }

}
