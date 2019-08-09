package com.huwei.gmall0416.payment.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.huwei.gmall0416.bean.PaymentInfo;
import com.huwei.gmall0416.payment.mapper.PaymentInfoMapper;
import com.huwei.gmall0416.payment.service.PaymentService;
import com.huwei.gmall0416.config.ActiveMQUtil;
import org.apache.activemq.command.ActiveMQMapMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsMessagingTemplate;
import tk.mybatis.mapper.entity.Example;

import javax.jms.*;
import java.util.List;

@Service
public class PaymentServiceImpl implements PaymentService {
    @Autowired
    PaymentInfoMapper paymentInfoMapper;

    @Autowired
    private  ActiveMQUtil activeMQUtil;
    @Autowired
    private JmsMessagingTemplate jmsMessagingTemplate;

    @Override
    public void savePaymentInfo(PaymentInfo paymentInfo) {
        // 根据订单id查询信息
        PaymentInfo paymentInfoQuery = new  PaymentInfo();
        paymentInfoQuery.setOrderId(paymentInfo.getOrderId());
        List<PaymentInfo> list = paymentInfoMapper.select(paymentInfoQuery);
         if (list.size()>0){
            return;
         }
         paymentInfoMapper.insertSelective(paymentInfo);
    }

    @Override
    public PaymentInfo getpaymentInfo(PaymentInfo paymentInfo){
        return   paymentInfoMapper.selectOne(paymentInfo);
    }

    @Override
    public void updatePaymentInfo(String outTradeNo, PaymentInfo paymentInfo) {
        Example example = new Example(PaymentInfo.class);
        example.createCriteria().andEqualTo("outTradeNo",outTradeNo);
        paymentInfoMapper.updateByExampleSelective(paymentInfo,example);
    }

    @Override
    public void sendPaymentResult(PaymentInfo paymentInfo, String result) {
        Connection connection = activeMQUtil.getConnection();
        try {
             connection.start();
            Session session = connection.createSession(true, Session.SESSION_TRANSACTED);
            // 创建队列
            Queue paymentResultQueue = session.createQueue("PAYMENT_RESULT_QUEUE");

            //添加队列
            MessageProducer producer = session.createProducer(paymentResultQueue);
            MapMessage mapMessage = new ActiveMQMapMessage();
            mapMessage.setString("orderId",paymentInfo.getOrderId());
            mapMessage.setString("result",result);
            producer.send(mapMessage);
            session.commit();
            producer.close();
            session.close();
            connection.close();
        }catch (JMSException e){
             e.printStackTrace();
         }
       }
}
