dependencies {
    // Testcontainers dependencies
    testImplementation 'org.testcontainers:testcontainers:1.17.6'
    testImplementation 'org.testcontainers:junit-jupiter:1.17.6'
    testImplementation 'org.testcontainers:kafka:1.17.6'
    testImplementation 'org.testcontainers:docker-compose:1.17.6'

    // Kafka dependencies
    implementation 'org.apache.kafka:kafka-clients:3.3.1'

    // JUnit dependencies
    testImplementation 'org.junit.jupiter:junit-jupiter-api:5.8.2'
    testRuntimeOnly 'org.junit.jupiter:junit-jupiter-engine:5.8.2'
}


 private static final String DOCKER_COMPOSE_FILE = "src/test/resources/docker-compose.yml";

    private static DockerComposeContainer<?> composeContainer;
    private static KafkaProducer<String, String> producer;

    @BeforeAll
    public static void setUp() {
        // Start Docker Compose container
        composeContainer = new DockerComposeContainer<>(new File(DOCKER_COMPOSE_FILE))
                .withExposedService("kafka_1", 9092, Wait.forListeningPort())
                .withLocalCompose(true);
        composeContainer.start();

        // Set up the producer
        Properties producerProps = new Properties();
        producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        producer = new KafkaProducer<>(producerProps);
    }
