package com.huwei.gmall0416.service;

import com.huwei.gmall0416.bean.CartInfo;

import java.util.List;

public interface CartService {

    //添加商品
    public  void  addToCart(String skuId,String userId,Integer skuNum);

    //从redis中获取购物车商品
    public List<CartInfo> getCartList(String userId);

    // 缓存中没有数据，则从 数据库中加载
    public List<CartInfo> loadCartCache(String userId);

    //cookie和reids数据合并购物车
    public List<CartInfo> mergeToCartList(List<CartInfo> cartListFromCookie, String userId);

    //购物车选中
    public void checkCart(String skuId, String isChecked, String userId);
    //得到选中的购物车列表
    List<CartInfo> getCartCheckedList(String userId);



}
