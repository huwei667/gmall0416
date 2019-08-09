package com.huwei.gmall0416.payment.producer;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQTextMessage;

import javax.jms.*;


public class ProducerTest {
    public static void main(String[] args) throws JMSException {
        //创建连接工厂
        ConnectionFactory connectionFactory=new ActiveMQConnectionFactory("tcp://192.168.24.135:61616");
        Connection connection = connectionFactory.createConnection();
        connection.start();
        //创建session第一个参数表示是否支持事务,false时,第二个参数
        //Session.AUTO_ACKNOWLEDGE，Session.CLIENT_ACKNOWLEDGE，DUPS_OK_ACKNOWLEDGE其中一个
       // 第一个参数设置为true时，第二个参数可以忽略 服务器设置为SESSION_TRANSACTED
        Session session=connection.createSession(false,Session.AUTO_ACKNOWLEDGE);
        //创建对列
        Queue queue=session.createQueue("huwei");
        //producer : 提供者
        MessageProducer producer=session.createProducer(queue);
        //创建消息对象
        ActiveMQTextMessage activeMQTextMessage=new ActiveMQTextMessage();
        activeMQTextMessage.setText("hello ActiveMq!");
        //发送消息
        producer.send(activeMQTextMessage);
        producer.close();
        connection.close();
        System.out.println("踩踩踩踩踩踩踩踩踩踩踩踩踩踩踩踩踩踩踩");

    }
}
