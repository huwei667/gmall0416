package com.huwei.gmall0416.order.controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.huwei.gmall0416.bean.CartInfo;
import com.huwei.gmall0416.bean.OrderDetail;
import com.huwei.gmall0416.bean.OrderInfo;
import com.huwei.gmall0416.bean.UserAddress;
import com.huwei.gmall0416.bean.enums.OrderStatus;
import com.huwei.gmall0416.bean.enums.ProcessStatus;
import com.huwei.gmall0416.config.LoginRequire;
import com.huwei.gmall0416.service.CartService;
import com.huwei.gmall0416.service.OrderService;
import com.huwei.gmall0416.service.UserInfoService;
import org.apache.catalina.servlet4preview.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;

@Controller
public class OrderController {

   //@Autowired
    @Reference
    private UserInfoService userInfoService;

    @Reference
    private CartService cartService;

    @Reference
    private OrderService orderService;

    @RequestMapping(value = "trade",method = RequestMethod.GET)
    @LoginRequire(autoRedirect =true)
    public String taradeInit(HttpServletRequest request){

      String userId=(String)request.getAttribute("userId");

      //得到选中的购物车列表
        List<CartInfo> cartCheckedList=cartService.getCartCheckedList(userId);
        //收货人地址
        List<UserAddress> userAddressList = userInfoService.getUserAddressList(userId);
        // 订单信息集合
        List<OrderDetail> orderDetailList=new ArrayList<>(cartCheckedList.size());
        for (CartInfo cartInfo: cartCheckedList){
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setSkuId(cartInfo.getSkuId());
            orderDetail.setSkuName(cartInfo.getSkuName());
            orderDetail.setImgUrl(cartInfo.getImgUrl());
            orderDetail.setSkuNum(cartInfo.getSkuNum());
            orderDetail.setOrderPrice(cartInfo.getCartPrice());
            orderDetailList.add(orderDetail);
        }
        OrderInfo orderInfo=new OrderInfo ();
        orderInfo.setOrderDetailList(orderDetailList);
        orderInfo.sumTotalAmount();
        request.setAttribute("totalAmount",orderInfo.getTotalAmount());
        request.setAttribute("orderDetailList",orderDetailList);
        request.setAttribute("userAddressList",userAddressList);
        // 将流水号存储
        request.setAttribute("tradeCode",orderService.getTradeNo(userId));
        return "trade";
    }

    @RequestMapping(value = "submitOrder",method = RequestMethod.POST)
    @LoginRequire(autoRedirect = true)
    public String submitOrder(OrderInfo orderInfo,HttpServletRequest request){
       // 检查tradeCode
        String userId = (String) request.getAttribute("userId");
        // 先获取页面传递过来的tradeCode
        String tradeNo = request.getParameter("tradeNo");
        // 做比较
        boolean flag = orderService.checkTradeCode(userId, tradeNo);
        if (!flag){
            request.setAttribute("errMsg","该页面已失效，请重新提交!");
            return "tradeFail";
        }
        // 初始化参数
        orderInfo.setOrderStatus(OrderStatus.UNPAID);
        orderInfo.setProcessStatus(ProcessStatus.UNPAID);
        orderInfo.sumTotalAmount();
        orderInfo.setUserId(userId);
        // 保存
        String orderId = orderService.saveOrder(orderInfo);
        // 删除tradeNo
        orderService.delTradeCode(userId);
        //重定向
        return "redirect://payment.gmall.com/index?orderId="+orderId;
    }


    /*@RequestMapping("trade")
    @ResponseBody
    public List<UserAddress> trade(HttpServletRequest request){

         String userId = request.getParameter("userId");

         List<UserAddress> userAddressList=userInfoService.getUserAddressList(userId);

         return  userAddressList;
    }*/
}
