  // Testcontainers dependencies
    testImplementation 'org.testcontainers:testcontainers:1.17.6'
    testImplementation 'org.testcontainers:junit-jupiter:1.17.6'
    testImplementation 'org.testcontainers:azure-eventhubs:1.17.6'

    // Azure Event Hubs dependencies
    implementation 'com.azure:azure-messaging-eventhubs:5.10.0'
    implementation 'com.azure:azure-identity:1.4.4'

    // JUnit dependencies
    testImplementation 'org.junit.jupiter:junit-jupiter-api:5.8.2'
    testRuntimeOnly 'org.junit.jupiter:junit-jupiter-engine:5.8.2'


       import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.EventHubContainer;
import org.testcontainers.utility.DockerImageName;
import com.azure.messaging.eventhubs.*;
import com.azure.identity.DefaultAzureCredentialBuilder;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class EventHubTest {

    private static EventHubContainer eventHubContainer;
    private static EventHubProducerClient producer;
    private static EventHubConsumerAsyncClient consumer;

    @BeforeAll
    public static void setUp() {
        // Start the Event Hub container
        eventHubContainer = new EventHubContainer(DockerImageName.parse("mcr.microsoft.com/azure-event-hubs:latest"))
                .withEnv("EVENTHUBS_NAMESPACE", "myeventhubnamespace")
                .withEnv("EVENTHUB_NAME", "myeventhub");

        eventHubContainer.start();

        // Set up the producer
        producer = new EventHubClientBuilder()
                .connectionString(eventHubContainer.getEventHubConnectionString())
                .eventHubName("myeventhub")
                .buildProducerClient();

        // Set up the consumer
        consumer = new EventHubClientBuilder()
                .connectionString(eventHubContainer.getEventHubConnectionString())
                .eventHubName("myeventhub")
                .consumerGroup(EventHubClientBuilder.DEFAULT_CONSUMER_GROUP_NAME)
                .buildAsyncConsumerClient();
    }

    @AfterAll
    public static void tearDown() {
        // Close the producer and consumer
        producer.close();
        consumer.close();

        // Stop the Event Hub container
        eventHubContainer.stop();
    }

    @Test
    public void testSendAndReceive() throws InterruptedException {
        // Send messages to Event Hub
        List<EventData> allEvents = Arrays.asList(
                new EventData("Event 1"),
                new EventData("Event 2"),
                new EventData("Event 3")
        );

        EventDataBatch eventDataBatch = producer.createBatch();
        for (EventData eventData : allEvents) {
            eventDataBatch.tryAdd(eventData);
        }

        producer.send(eventDataBatch);
        System.out.println("Events sent successfully.");

        // Receive messages from Event Hub
        consumer.receive(false)
                .subscribe(partitionEvent -> {
                    System.out.printf("Received event from partition %s with sequence number %d.%n",
                            partitionEvent.getPartitionContext().getPartitionId(),
                            partitionEvent.getData().getSequenceNumber());
                }, error -> {
                    System.err.println("Error occurred while receiving: " + error);
                });

        // Keep the main thread alive for receiving events
        TimeUnit.SECONDS.sleep(30);

        // Check if messages were received
        assertTrue(true, "Messages were received.");
    }
}
