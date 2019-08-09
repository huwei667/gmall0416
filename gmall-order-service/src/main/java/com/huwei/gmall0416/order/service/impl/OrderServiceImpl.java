package com.huwei.gmall0416.order.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.huwei.gmall0416.bean.OrderDetail;
import com.huwei.gmall0416.bean.OrderInfo;
import com.huwei.gmall0416.bean.enums.ProcessStatus;
import com.huwei.gmall0416.config.ActiveMQUtil;
import com.huwei.gmall0416.config.RedisUtil;
import com.huwei.gmall0416.order.mapper.OrderDetailMapper;
import com.huwei.gmall0416.order.mapper.OrderInfoMapper;
import com.huwei.gmall0416.service.OrderService;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import java.util.*;

@Service
public class OrderServiceImpl  implements OrderService {

    @Autowired
    private OrderInfoMapper orderInfoMapper;

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private ActiveMQUtil activeMQUtil;

    @Override
    public String saveOrder(OrderInfo orderInfo) {
         //设置创建时间
        orderInfo.setCreateTime(new Date());
        //设置失效时间
        Calendar calendar=Calendar.getInstance();
        calendar.add(Calendar.DATE,1);
        orderInfo.setExpireTime(calendar.getTime());
        // 生成第三方支付编号
        String outTradeNo="ATGUIGU"+System.currentTimeMillis()+""+new Random().nextInt(1000);
        orderInfo.setOutTradeNo(outTradeNo);
        orderInfoMapper.insertSelective(orderInfo);
        // 插入订单详细信息
        List<OrderDetail> orderDetailList=orderInfo.getOrderDetailList();
        for (OrderDetail orderDetail : orderDetailList) {
            orderDetail.setOrderId(orderInfo.getId());
            orderDetailMapper.insertSelective(orderDetail);
        }
        // 为了跳转到支付页面使用。支付会根据订单id进行支付。
        String orderId = orderInfo.getId();
        return orderId;
    }
     // 生成流水号
     public  String getTradeNo(String userId) {
         Jedis jedis = redisUtil.getJedis();
         //key
         String tradeNoKey="user:"+userId+":tradeCode";
         String tradeCode = UUID.randomUUID().toString();
         jedis.setex(tradeNoKey,10*60,tradeCode);
         jedis.close();
         return tradeCode;
     }
    // 验证流水号
    public  boolean checkTradeCode(String userId,String tradeCodeNo) {
        Jedis jedis = redisUtil.getJedis();
        String tradeNoKey = "user:"+userId+":tradeCode";
        String tradeCode = jedis.get(tradeNoKey);
        jedis.close();
        if (tradeCodeNo!=null && tradeCodeNo.equals(tradeCode)){
            return  true;
        }else{
            return false;
        }

    }
    // 删除流水号
    public void  delTradeCode(String userId) {
        Jedis jedis = redisUtil.getJedis();
        String tradeNoKey =  "user:"+userId+":tradeCode";
        jedis.del(tradeNoKey);
        jedis.close();
    }

    @Override
    public OrderInfo getOrderInfo(String orderId) {
        // 需要根据orderId 查询orderDetail
        OrderDetail orderDetail = new OrderDetail();
        orderDetail.setOrderId(orderId);
        List<OrderDetail> orderDetails = orderDetailMapper.select(orderDetail);

        OrderInfo orderInfo = orderInfoMapper.selectByPrimaryKey(orderId);
        // 将查询出来的orderDetail集合放入orderInfo中
        orderInfo.setOrderDetailList(orderDetails);
        return orderInfo;
    }

    @Override
    public void updateOrderStatus(String orderId, ProcessStatus paid) {

        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setId(orderId);
        System.out.println("paid===="+paid);
        orderInfo.setProcessStatus(paid);
        // 修改订单的状态
        orderInfo.setOrderStatus(paid.getOrderStatus());
        System.out.println("getOrderStatus===="+paid.getOrderStatus());
        orderInfoMapper.updateByPrimaryKeySelective(orderInfo);
    }

    @Override
    public void sendOrderStatus(String orderId) {
        // 创建工厂
        Connection connection = activeMQUtil.getConnection();
        // 得到Json字符串
        String orderJson = initWareOrder(orderId);
        // 打开工厂
        try {
            connection.start();
            // 创建session
            Session session = connection.createSession(true, Session.SESSION_TRANSACTED);
            // 创建队列
            Queue order_result_queue = session.createQueue("ORDER_RESULT_QUEUE");
            // 创建提供者
            MessageProducer producer = session.createProducer(order_result_queue);

            ActiveMQTextMessage activeMQTextMessage = new ActiveMQTextMessage();
            // 得到的字符串：有orderInfo的基本信息以及OrderDetail详细信息 组成的json字符串
            // 将上述数据封装成一个map对象，使用fastJson 将map 转换为一个字符串即可！
            activeMQTextMessage.setText(orderJson);
            producer.send(activeMQTextMessage);
            session.commit();

            // 关闭操作
            producer.close();
            session.close();
            connection.close();
        } catch (JMSException e) {
            e.printStackTrace();
        }


    }

    private String initWareOrder(String orderId) {
        OrderInfo orderInfo = getOrderInfo(orderId);
        Map map = initWareOrder(orderInfo);
        // 将map转换为字符串
        return   JSON.toJSONString(map);
    }

    private Map initWareOrder(OrderInfo orderInfo) {
        // 创建一个map对象
        Map map = new HashMap<>();
        map.put("orderId",orderInfo.getId());

        map.put("consignee", orderInfo.getConsignee());
        map.put("consigneeTel",orderInfo.getConsigneeTel());
        map.put("orderComment",orderInfo.getOrderComment());
        map.put("orderBody","发送消息给库存系统，减库存");
        map.put("deliveryAddress",orderInfo.getDeliveryAddress());
        map.put("paymentWay","2");
//      拆单需要使用仓库的Id
//        map.put("wareId",orderInfo.getWareId());
        // details ：orderDetail
        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        // 声明一个集合来存储订单详情信息
        ArrayList<Object> arrayList = new ArrayList<>();
        // 循环遍历
        for (OrderDetail orderDetail : orderDetailList) {
            Map newMap =   new HashMap<>();
            newMap.put("skuId",orderDetail.getSkuId());
            newMap.put("skuName",orderDetail.getSkuName());
            newMap.put("skuNum",orderDetail.getSkuNum());
            arrayList.add(newMap);
        }
        map.put("details",arrayList);

        return map;
    }

}
