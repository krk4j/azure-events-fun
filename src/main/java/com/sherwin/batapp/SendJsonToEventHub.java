import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;
import javax.jms.ConnectionFactory;
import com.ibm.mq.jms.MQConnectionFactory;
import com.ibm.msg.client.wmq.WMQConstants;

@Configuration
@EnableJms
public class MqConfig {

    @Bean
    public GenericContainer<?> mqContainer() {
        GenericContainer<?> container = new GenericContainer<>(DockerImageName.parse("ibmcom/mq:latest"))
                .withExposedPorts(1414)
                .withEnv("LICENSE", "accept")
                .withEnv("MQ_QMGR_NAME", "QM1")
                .withEnv("MQ_APP_PASSWORD", "passw0rd");
        container.start();
        return container;
    }

    @Bean
    public ConnectionFactory connectionFactory(GenericContainer<?> mqContainer) throws Exception {
        MQConnectionFactory connectionFactory = new MQConnectionFactory();
        connectionFactory.setHostName(mqContainer.getHost());
        connectionFactory.setPort(mqContainer.getMappedPort(1414));
        connectionFactory.setQueueManager("QM1");
        connectionFactory.setChannel("DEV.APP.SVRCONN");
        connectionFactory.setTransportType(WMQConstants.WMQ_CM_CLIENT);
        connectionFactory.setStringProperty(WMQConstants.USERID, "app");
        connectionFactory.setStringProperty(WMQConstants.PASSWORD, "passw0rd");
        return connectionFactory;
    }
}






---------------


    import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Component
public class MqListener {

    @JmsListener(destination = "DEV.QUEUE.1")
    public void receiveMessage(String message) {
        System.out.println("Received message: " + message);
    }
}



-------------------------


    import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

@Service
public class MqSender {

    private final JmsTemplate jmsTemplate;

    public MqSender(JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
    }

    public void sendMessage(String message) {
        jmsTemplate.convertAndSend("DEV.QUEUE.1", message);
    }
}


------------------------

    import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class MqTest {

    @Autowired
    private MqSender mqSender;

    @Test
    public void testSendAndReceive() throws InterruptedException {
        mqSender.sendMessage("Hello IBM MQ!");

        // Wait for the message to be received
        Thread.sleep(2000);
    }
}
