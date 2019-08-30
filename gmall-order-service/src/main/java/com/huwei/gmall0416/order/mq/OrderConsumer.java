package com.huwei.gmall0416.order.mq;

import com.alibaba.dubbo.config.annotation.Reference;
import com.huwei.gmall0416.bean.enums.ProcessStatus;
import com.huwei.gmall0416.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.MapMessage;

// 消费消息
@Component
public class OrderConsumer {

    @Reference
    private OrderService orderService;


    //监控订单消息
    @JmsListener(destination="PAYMENT_RESULT_QUEUE",containerFactory = "jmsQueueListener")
    public void  consumerPaymentResult(MapMessage mapMessage) throws JMSException {
        // 取得消息队列中的数据
        String orderId = mapMessage.getString("orderId");
        String result = mapMessage.getString("result");
        // 支付成功！
        if ("success".equals(result)){
            // 修改订单状态
            //PAID ：已支付
            orderService.updateOrderStatus(orderId, ProcessStatus.PAID);
            // 通知减库存
            orderService.sendOrderStatus(orderId);
            //NOTIFIED_WARE
            orderService.updateOrderStatus(orderId, ProcessStatus.NOTIFIED_WARE);
        }else {
            orderService.updateOrderStatus(orderId,ProcessStatus.UNPAID);
        }
      }
    @JmsListener(destination="SKU_DEDUCT_QUEUE",containerFactory = "jmsQueueListener")
    public void consumeSkuDeduct(MapMessage mapMessage) throws JMSException {
        String orderId = mapMessage.getString("orderId");
        String  status = mapMessage.getString("status");
        if("DEDUCTED".equals(status)){
            orderService.updateOrderStatus( orderId , ProcessStatus.DELEVERED);
        }else{
            orderService.updateOrderStatus( orderId , ProcessStatus.STOCK_EXCEPTION);
        }
    }
}
