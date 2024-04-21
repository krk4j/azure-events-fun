package com.sherwin.batapp;

import com.microsoft.azure.functions.annotation.*;
import com.microsoft.azure.functions.*;

public class EventHubReceiverFunction {

    @FunctionName("EventHubReceiverFunction")
    public void run(
            @EventHubTrigger(
                    name = "soevent",
                    eventHubName = "soevents",
                    connection = "EventHubConnectionAppSetting",
                    consumerGroup = "$Default"
            ) String message,
            final ExecutionContext context
    ) {
        context.getLogger().info("Java Event Hub receiver function processed a message: " + message);
        // Process the received message here
    }
}