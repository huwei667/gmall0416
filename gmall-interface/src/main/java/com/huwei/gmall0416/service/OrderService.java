package com.huwei.gmall0416.service;

import com.huwei.gmall0416.bean.OrderInfo;
import com.huwei.gmall0416.bean.enums.ProcessStatus;

public interface  OrderService {
   //接口：返回orderId，保存完，应该调到支付，根据orderId。
    public  String  saveOrder(OrderInfo orderInfo);

    // 生成流水号
    public  String getTradeNo(String userId);

    // 验证流水号
    public  boolean checkTradeCode(String userId,String tradeCodeNo) ;

    // 删除流水号
    public void  delTradeCode(String userId);

    //根据订单id获取订单
    public OrderInfo getOrderInfo(String orderId);

    //消费支付订单
    void updateOrderStatus(String orderId, ProcessStatus paid);
    //减库存
    public void sendOrderStatus(String orderId);
}
