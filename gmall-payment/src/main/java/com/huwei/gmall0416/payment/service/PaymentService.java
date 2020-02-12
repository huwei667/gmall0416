package com.huwei.gmall0416.payment.service;

import com.huwei.gmall0416.bean.PaymentInfo;

public interface PaymentService {
    //保存支付订单
    public void savePaymentInfo(PaymentInfo paymentInfo);

    PaymentInfo getpaymentInfo(PaymentInfo paymentInfo);

    void updatePaymentInfo(String out_trade_no, PaymentInfo paymentInfoUpd);

    //手动支付成功调用接口
    public void sendPaymentResult(PaymentInfo paymentInfo,String result);

    // 查询支付是否成功
    public boolean checkPayment(PaymentInfo paymentInfoQuery) ;
}
