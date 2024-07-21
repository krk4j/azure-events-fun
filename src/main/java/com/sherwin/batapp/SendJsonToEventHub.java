@DynamicPropertySource
static void registerMqProperties(DynamicPropertyRegistry registry) {
  registry.add("ibm.mq.connName", 
               () -> String.format("%s(%d)", 
                                   mqContainer.getHost(), 
                                   mqContainer.getFirstMappedPort()));
}
