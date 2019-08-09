package com.huwei.gmall0416.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.huwei.gmall0416.bean.BaseAttrInfo;
import com.huwei.gmall0416.bean.SkuInfo;
import com.huwei.gmall0416.bean.SkuLsInfo;
import com.huwei.gmall0416.service.ListService;
import com.huwei.gmall0416.service.ManageService;
import org.apache.commons.beanutils.BeanUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.lang.reflect.InvocationTargetException;

@Controller
public class AttrManageController {

    @Reference
    private ManageService manageService;

    @Reference
    private ListService listService;

    @RequestMapping(value = "saveAttrInfo", method = RequestMethod.POST)
    @ResponseBody
    public String saveAttrInfo(BaseAttrInfo baseAttrInfo) {
        manageService.saveAttrInfo(baseAttrInfo);
        return "success";
    }

    //保存sku商品信息到es
    @RequestMapping(value = "onSale",method=RequestMethod.GET)
    @ResponseBody
    public void onSale(String skuId) {
        SkuInfo skuInfo=manageService.getSkuInfo(skuId);
        SkuLsInfo skuLsInfo=new SkuLsInfo();
        //属性拷贝
        try {
            BeanUtils.copyProperties(skuLsInfo,skuInfo);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        listService.saveSkuInfo(skuLsInfo);
    }
}
