package com.sherwin.batapp;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.functions.annotation.*;
import com.microsoft.azure.functions.*;
import com.sherwin.batapp.model.Payload;

import java.util.*;
import java.util.logging.Logger;

public class EventHubSenderFunction {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @FunctionName("EventHubSenderFunction")
    public void run(
            @HttpTrigger(name = "req", methods = {HttpMethod.POST}, authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<String> request,
            @EventHubOutput(
                    name = "soevent",
                    eventHubName = "soevents",
                    connection = "EventHubConnectionAppSetting"
            ) OutputBinding<String> outputEventHub,
            final ExecutionContext context
    ) {
        context.getLogger().info("Java Event Hub sender function executed.");

        String requestData = request.getBody();
        if (requestData == null || requestData.isEmpty()) {
            context.getLogger().warning("Received empty request body.");
            return;
        }

        try {
            // Parse JSON data
            Payload dataObject = objectMapper.readValue(requestData, Payload.class);
            String jsonMessage = objectMapper.writeValueAsString(dataObject);

            // Send JSON message to Event Hub
            outputEventHub.setValue(jsonMessage);
            context.getLogger().info("Message sent to Event Hub: " + jsonMessage);
        } catch (Exception e) {
            context.getLogger().severe("Error processing request: " + e.getMessage());
            e.printStackTrace();
        }
    }
}