implementation 'com.ibm.mq:com.ibm.mq.allclient:9.2.3.0'


    import com.ibm.mq.MQQueueManager;
import com.ibm.mq.constants.CMQC;
import com.ibm.mq.jms.MQConnectionFactory;
import javax.jms.JMSException;
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
                .withEnv("MQ_QMGR_NAME", "QM1")) {

            mqContainer.start();

            String mqHost = mqContainer.getHost();
            Integer mqPort = mqContainer.getMappedPort(1414);

            System.out.println("IBM MQ is running at " + mqHost + ":" + mqPort);

            MQConnectionFactory connectionFactory = new MQConnectionFactory();
            connectionFactory.setHostName(mqHost);
            connectionFactory.setPort(mqPort);
            connectionFactory.setQueueManager("QM1");
            connectionFactory.setChannel("DEV.APP.SVRCONN");
            connectionFactory.setTransportType(CMQC.MQJMS_TP_CLIENT_MQ_TCPIP);

            QueueConnection queueConnection = connectionFactory.createQueueConnection();
            queueConnection.start();

            QueueSession queueSession = queueConnection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
            Queue queue = queueSession.createQueue("DEV.QUEUE.1");
            QueueSender queueSender = queueSession.createSender(queue);

            TextMessage message = queueSession.createTextMessage("Hello IBM MQ!");
            queueSender.send(message);

            queueConnection.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
