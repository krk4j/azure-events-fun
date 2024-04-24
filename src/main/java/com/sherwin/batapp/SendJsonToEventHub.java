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

    public static void main(String[] args) {
        // Define AVRO schema
        String avroSchema = "{\"type\": \"record\", \"name\": \"Example\", \"fields\": [{\"name\": \"field1\", \"type\": \"string\"}, {\"name\": \"field2\", \"type\": \"int\"}]}";

        // Convert JSON to AVRO
        String jsonString = "{\"field1\": \"value1\", \"field2\": 42}";
        byte[] avroData = convertJsonToAvro(jsonString, avroSchema);

        // Event Hubs connection string
        String connectionString = "<YOUR_EVENT_HUB_CONNECTION_STRING>";
        String eventHubName = "<YOUR_EVENT_HUB_NAME>";

        // Create a producer client
        EventHubProducerClient producer = new EventHubClientBuilder()
                .connectionString(connectionString, eventHubName)
                .buildProducerClient();

        // Send AVRO data to Event Hub
        EventData eventData = new EventData(avroData);
        producer.send(eventData);

        // Close the producer client
        producer.close();
    }

    private static byte[] convertJsonToAvro(String jsonString, String avroSchema) {
        try {
            Schema schema = new Schema.Parser().parse(avroSchema);
            GenericRecord record = new GenericData.Record(schema);

            // Parse JSON data
            JSONObject jsonObject = new JSONObject(jsonString);
            for (Schema.Field field : schema.getFields()) {
                record.put(field.name(), jsonObject.get(field.name()));
            }

            // Serialize AVRO data
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            DatumWriter<GenericRecord> datumWriter = new SpecificDatumWriter<>(schema);
            Encoder encoder = EncoderFactory.get().binaryEncoder(outputStream, null);
            datumWriter.write(record, encoder);
            encoder.flush();
            outputStream.close();
            return outputStream.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
