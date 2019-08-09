package com.huwei.gmall0416.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.huwei.gmall0416.bean.*;
import com.huwei.gmall0416.service.ManageService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

@Controller
public class ManageController {

    @Reference
    private ManageService manageService;

    @RequestMapping(value="index" )
    public String index(){
        return "index";
    }

    @RequestMapping("attrListPage")
    public String getAttrListPage(){
        return "attrListPage";
    }


    /***
     * 获得一级分类
     * @return
     */
    @RequestMapping("getCatalog1")
    @ResponseBody
    public List<BaseCatalog1> getCatalog1(){
          List<BaseCatalog1> catalog1List = manageService.getCatalog1();
        return catalog1List;
    }
    /***
     * 获得二级分类
     * @param map
     * @return
     */
    @RequestMapping("getCatalog2")
    @ResponseBody
    public List<BaseCatalog2> getCatalog2(@RequestParam Map<String,String> map){
        String catalog1Id =   map.get("catalog1Id") ;
        List<BaseCatalog2> catalog2List = manageService.getCatalog2(catalog1Id);
        return catalog2List;
    }

    /***
     * 获得三级分类
     * @param map
     * @return
     */
    @RequestMapping("getCatalog3")
    @ResponseBody
    public List<BaseCatalog3> getCatalog3(@RequestParam Map<String,String> map){
        String catalog2Id =   map.get("catalog2Id") ;
        List<BaseCatalog3> catalog3List = manageService.getCatalog3(catalog2Id);
        return catalog3List;
    }
    /***
     * 获得属性列表
     * @param //map
     * @return
     */
 /*   @RequestMapping("attrInfoList")
    @ResponseBody
    public List<BaseAttrInfo>  attrInfoList (@RequestParam Map<String,String> map){
        String catalog3Id =   map.get("catalog3Id") ;
        List<BaseAttrInfo> attrList = manageService.getAttrList(catalog3Id);
        return attrList;
    }*/
    @RequestMapping("attrInfoList")
    @ResponseBody
    public List<BaseAttrInfo>  attrInfoList (String  catalog3Id){ ;
        List<BaseAttrInfo> attrList = manageService.getAttrList(catalog3Id);
        return attrList;
    }
    //编辑
    @RequestMapping("getAttrValueList")
    @ResponseBody
    public List<BaseAttrValue> getAttrValueList(String attrId){
         BaseAttrInfo attrInfo = manageService.getAttrInfo(attrId);
        return attrInfo.getAttrValueList();
    }
    //基本销售属性表
    @RequestMapping("baseSaleAttrList")
    @ResponseBody
    public List<BaseSaleAttr> getBaseSaleAttrList(){
        return   manageService.getBaseSaleAttrList();
    }


}
