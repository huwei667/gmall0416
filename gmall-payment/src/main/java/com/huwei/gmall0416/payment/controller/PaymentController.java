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
        paymentInfo.setPaymentStatus(PaymentStatus.UNPAID);
        // 保存信息
        paymentService.savePaymentInfo(paymentInfo);
        // 支付宝参数

        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();//创建API对应的request
        //  http://payment.gmall.com/alipay/callback/return
        alipayRequest.setReturnUrl(AlipayConfig.return_payment_url);
        alipayRequest.setNotifyUrl(AlipayConfig.notify_payment_url);//在公共参数中设置回跳和通知地址
        // 声明一个Map
        Map<String,Object> bizContnetMap=new HashMap<>();
        bizContnetMap.put("out_trade_no",paymentInfo.getOutTradeNo());
        bizContnetMap.put("product_code","FAST_INSTANT_TRADE_PAY");
        bizContnetMap.put("subject",paymentInfo.getSubject());
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
        response.setContentType("text/html;charset=UTF-8");
        return form;
     }

    @RequestMapping(value = "/alipay/callback/return",method = RequestMethod.GET)
    public String callbackReturn(){
        System.out.println("着反对撒顶顶顶顶顶顶顶顶顶顶顶顶顶顶顶");
        return "redirect://"+AlipayConfig.return_order_url;
    }

    @RequestMapping(value = "/alipay/callback/notify",method = RequestMethod.POST)
    @ResponseBody
    public String paymentNotify(@RequestParam Map<String,String> paramMap, HttpServletRequest request) throws AlipayApiException {
        // 拿公用key+数据验证
        String sign = request.getParameter("sign");
        boolean flag = AlipaySignature.rsaCheckV1(paramMap, AlipayConfig.alipay_public_key,"utf-8",AlipayConfig.sign_type);
        if (!flag){
            return "fial";
        }
        // 判断结束
        String trade_status = paramMap.get("trade_status");
        if ("TRADE_SUCCESS".equals(trade_status) || "TRADE_FINISHED".equals(trade_status)){
            // 查单据是否处理
            String out_trade_no = paramMap.get("out_trade_no");
            PaymentInfo paymentInfo = new PaymentInfo();
            paymentInfo.setOutTradeNo(out_trade_no);
            PaymentInfo paymentInfoHas = paymentService.getpaymentInfo(paymentInfo);

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
                paymentService.updatePaymentInfo(out_trade_no,paymentInfoUpd);
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
        return "sent payment result";
    }
}