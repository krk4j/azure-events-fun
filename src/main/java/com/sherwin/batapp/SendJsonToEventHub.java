    public String readXml() throws IOException {
        Resource resource = resourceLoader.getResource("classpath:data.xml");
        return new String(Files.readAllBytes(Paths.get(resource.getURI())), StandardCharsets.UTF_8);
    }
