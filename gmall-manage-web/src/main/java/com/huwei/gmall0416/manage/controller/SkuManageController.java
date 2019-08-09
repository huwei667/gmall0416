package com.huwei.gmall0416.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.huwei.gmall0416.bean.SkuInfo;
import com.huwei.gmall0416.bean.SpuImage;
import com.huwei.gmall0416.bean.SpuSaleAttr;
import com.huwei.gmall0416.service.ManageService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class SkuManageController {

    @Reference
    private ManageService manageService;

    @RequestMapping("spuImageList")
    @ResponseBody
    public List<SpuImage> spuImageList(String spuId) {
        return manageService.getSpuImageList(spuId);
    }

    @RequestMapping("spuSaleAttrList")
    @ResponseBody
    public List<SpuSaleAttr> getspuSaleAttrList(String spuId) {
        List<SpuSaleAttr> spuSaleAttrList = manageService.getSpuSaleAttrList(spuId);
        return spuSaleAttrList;
    }
    @RequestMapping(value = "saveSku",method = RequestMethod.POST)
    @ResponseBody
    public String saveSkuInfo(SkuInfo skuInfo){
        manageService.saveSkuInfo(skuInfo);
        return "success";
    }
}
