import com.ibm.mq.jms.MQQueueConnectionFactory;
import com.ibm.msg.client.wmq.WMQConstants;

import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TextMessage;

public class MqTest {
    private static final DockerImageName IBM_MQ_IMAGE = DockerImageName.parse("ibmcom/mq:latest");

    public static void main(String[] args) {
        try (GenericContainer<?> mqContainer = new GenericContainer<>(IBM_MQ_IMAGE)
                .withExposedPorts(1414)
                .withEnv("LICENSE", "accept")
                .withEnv("MQ_QMGR_NAME", "QM1")
                .withEnv("MQ_APP_PASSWORD", "passw0rd")) {

            mqContainer.start();

            String mqHost = mqContainer.getHost();
            Integer mqPort = mqContainer.getMappedPort(1414);

            System.out.println("IBM MQ is running at " + mqHost + ":" + mqPort);

            MQQueueConnectionFactory connectionFactory = new MQQueueConnectionFactory();
            connectionFactory.setHostName(mqHost);
            connectionFactory.setPort(mqPort);
            connectionFactory.setQueueManager("QM1");
            connectionFactory.setChannel("DEV.APP.SVRCONN");
            connectionFactory.setTransportType(WMQConstants.WMQ_CM_CLIENT);
            connectionFactory.setStringProperty(WMQConstants.USERID, "app");
            connectionFactory.setStringProperty(WMQConstants.PASSWORD, "passw0rd");

            QueueConnection queueConnection = connectionFactory.createQueueConnection();
            queueConnection.start();

            QueueSession queueSession = queueConnection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
            Queue queue = queueSession.createQueue("queue:///DEV.QUEUE.1");
            QueueSender queueSender = queueSession.createSender(queue);

            TextMessage message = queueSession.createTextMessage("Hello IBM MQ!");
            queueSender.send(message);

            System.out.println("Message sent to IBM MQ: " + message.getText());

            queueConnection.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
