package com.huwei.gmall0416.payment.producer;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;
import java.awt.font.TextMeasurer;

public class ConsumerTest {
    public static void main(String[] args) throws JMSException {
        ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory(ActiveMQConnection.DEFAULT_USER, ActiveMQConnection.DEFAULT_PASSWORD, "tcp://192.168.24.135:61616");
        // 创建连接
        Connection connection = activeMQConnectionFactory.createConnection();
        connection.start();
        // 创建会话
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        // 创建队列
        Queue queue = session.createQueue("huwei");
        // 创建Consumer
        MessageConsumer consumer = session.createConsumer(queue);
        TextMessage msg = (TextMessage) consumer.receive();
        System.out.println("收到的内容：" + msg.getText());
       /* // 接收消息
        consumer.setMessageListener(new MessageListener() {
            @Override
            public void onMessage(Message message) {
                // 参数就是收到的消息

                        TextMessage msg = (TextMessage)consumer.receive();

                }
            }
        });*/
        consumer.close();
        session.close();
    }
}
