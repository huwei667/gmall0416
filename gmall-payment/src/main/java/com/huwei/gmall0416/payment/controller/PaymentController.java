package com.huwei.gmall0416.payment.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.huwei.gmall0416.bean.OrderInfo;
import com.huwei.gmall0416.bean.PaymentInfo;
import com.huwei.gmall0416.bean.enums.PaymentStatus;
import com.huwei.gmall0416.config.LoginRequire;
import com.huwei.gmall0416.payment.config.AlipayConfig;
import com.huwei.gmall0416.payment.service.PaymentService;
import com.huwei.gmall0416.service.OrderService;
import org.apache.catalina.servlet4preview.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Controller
public class PaymentController {
    @Reference
    OrderService orderService;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    AlipayClient alipayClient;


    @RequestMapping("index")
    @LoginRequire(autoRedirect = true)
    public String index(HttpServletRequest request, Model model){
        // 获取订单的id
        String orderId = request.getParameter("orderId");
        OrderInfo orderInfo = orderService.getOrderInfo(orderId);
        model.addAttribute("orderId",orderId);
        model.addAttribute("totalAmount",orderInfo.getTotalAmount());
        return "index";
      }
    @RequestMapping(value = "/alipay/submit",method = RequestMethod.GET)
    @ResponseBody
    public String submitPayment(HttpServletRequest request, HttpServletResponse response) {
        // 获取订单Id
        String orderId = request.getParameter("orderId");
        // 取得订单信息
        OrderInfo orderInfo = orderService.getOrderInfo(orderId);
        // 保存支付信息
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setOrderId(orderId);
        paymentInfo.setOutTradeNo(orderInfo.getOutTradeNo());
        paymentInfo.setTotalAmount(orderInfo.getTotalAmount());
        paymentInfo.setSubject("小米7支付");
        //UNPAID :支付中
        paymentInfo.setPaymentStatus(PaymentStatus.UNPAID);
        paymentService.savePaymentInfo(paymentInfo);

        // 支付宝参数
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();//创建API对应的request
        //  http://payment.gmall.com/alipay/callback/return
        alipayRequest.setReturnUrl(AlipayConfig.return_payment_url); //同步
        alipayRequest.setNotifyUrl(AlipayConfig.notify_payment_url);//在公共参数中设置回跳和通知地址
        // 声明一个Map
        Map<String,Object> bizContnetMap=new HashMap<>();
        //out_trade_no:订单号
        bizContnetMap.put("out_trade_no",paymentInfo.getOutTradeNo());
        //FAST_INSTANT_TRADE_PAY不要更改
        bizContnetMap.put("product_code","FAST_INSTANT_TRADE_PAY");
        //商品名称
        bizContnetMap.put("subject",paymentInfo.getSubject());
        //支付价格
        bizContnetMap.put("total_amount",paymentInfo.getTotalAmount());
        // 将map变成json
        String Json = JSON.toJSONString(bizContnetMap);
        alipayRequest.setBizContent(Json);
        String form="";
        try {
            form = alipayClient.pageExecute(alipayRequest).getBody(); //调用SDK生成表单
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        //调用支付宝查询是否已经付款了  延迟队列
        //paymentService.sendDelayPaymentResult(paymentInfo.getOutTradeNo(),15, 3);
        response.setContentType("text/html;charset=UTF-8");
        return form;
     }
     //同步
    @RequestMapping(value = "/alipay/callback/return",method = RequestMethod.GET)
    public String callbackReturn(){
        return "redirect://"+AlipayConfig.return_order_url;
    }
    //异步
    @RequestMapping(value = "/alipay/callback/notify",method = RequestMethod.POST)
    @ResponseBody
    public String paymentNotify(@RequestParam Map<String,String> paramMap, HttpServletRequest request) throws AlipayApiException {
        // 拿公用key+数据验证
        String sign = request.getParameter("sign");
        /**
         * paramMap :数据
         * 公用key
         * 字符集
         * 算法
         */
        boolean flag = AlipaySignature.rsaCheckV1(paramMap, AlipayConfig.alipay_public_key,"utf-8",AlipayConfig.sign_type);
        if (!flag){
            return "fial";
        }
        // 判断结束
        String trade_status = paramMap.get("trade_status");
        if ("TRADE_SUCCESS".equals(trade_status) || "TRADE_FINISHED".equals(trade_status)){
            // 查单据是否处理
            //订单编号
            String out_trade_no = paramMap.get("out_trade_no");
            PaymentInfo paymentInfo = new PaymentInfo();
            paymentInfo.setOutTradeNo(out_trade_no);
            PaymentInfo paymentInfoHas = paymentService.getpaymentInfo(paymentInfo);
            // PAID:已支付   ClOSED:已关闭
            if (paymentInfoHas.getPaymentStatus()==PaymentStatus.PAID || paymentInfoHas.getPaymentStatus()==PaymentStatus.ClOSED){
                return "fail";
            }else {
                // 修改
                PaymentInfo paymentInfoUpd = new PaymentInfo();
                // 设置状态
                paymentInfoUpd.setPaymentStatus(PaymentStatus.PAID);
                // 设置创建时间
                paymentInfoUpd.setCallbackTime(new Date());
                // 设置内容
                paymentInfoUpd.setCallbackContent(paramMap.toString());
                //修改订单 outTradeNo
                paymentService.updatePaymentInfo(out_trade_no,paymentInfoUpd);
                //
                sendPaymentResult(paymentInfo,"success");
                return "success";
            }
        }
        return  "fail";
     }
    // 发送验证
    @RequestMapping("sendPaymentResult")
    @ResponseBody
    public String sendPaymentResult(PaymentInfo paymentInfo,@RequestParam("result") String result){
        paymentService.sendPaymentResult(paymentInfo,result);
        return "sentpaymentresult";
    }
    // 查询订单信息
    @RequestMapping("queryPaymentResult")
    @ResponseBody
    public String queryPaymentResult(PaymentInfo paymentInfo){
        //String orderId = request.getParameter("orderId");
        //查询支付订单
        paymentService.getpaymentInfo(paymentInfo);
        PaymentInfo paymentInfoQuery = new PaymentInfo();
       // paymentInfoQuery.setOrderId(orderId);
        boolean flag = paymentService.checkPayment(paymentInfoQuery);
        return ""+flag;
    }
}