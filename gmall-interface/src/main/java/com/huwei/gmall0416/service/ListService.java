package com.huwei.gmall0416.service;

import com.huwei.gmall0416.bean.SkuLsInfo;
import com.huwei.gmall0416.bean.SkuLsParams;
import com.huwei.gmall0416.bean.SkuLsResult;

public interface ListService {

    //报错sku商品信息
    public void saveSkuInfo(SkuLsInfo skuLsInfo);


    //显示商品信息
    public SkuLsResult search(SkuLsParams skuLsParams);

    //用来做redis缓存计数器
    public void incrHotScore(String skuId);

}
