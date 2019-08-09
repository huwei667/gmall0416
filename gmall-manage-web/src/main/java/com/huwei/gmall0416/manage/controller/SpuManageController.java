package com.huwei.gmall0416.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.huwei.gmall0416.bean.SpuInfo;
import com.huwei.gmall0416.service.ManageService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

@Controller
public class SpuManageController {
    @Reference
    private ManageService manageService;

    @RequestMapping("spuListPage")
     public String getSpuListPage(){
        return "spuListPage";
    }

    @RequestMapping("spuList")
    @ResponseBody
    public List<SpuInfo> getSpuInfoList(@RequestParam Map<String,String> map){
        String catalog3Id = map.get("catalog3Id");
        SpuInfo spuInfo =new SpuInfo();
        spuInfo.setCatalog3Id(catalog3Id);
        System.out.println("catalog3Id=="+catalog3Id);
        List<SpuInfo> spuInfoList = manageService.getSpuInfoList(spuInfo);
        return spuInfoList;
      }
    @RequestMapping(value = "saveSpuInfo",method = RequestMethod.POST)
    @ResponseBody
    public  String saveSpuInfo(SpuInfo spuInfo){
        manageService.saveSpuInfo(spuInfo);
        return  "success";
    }

}
