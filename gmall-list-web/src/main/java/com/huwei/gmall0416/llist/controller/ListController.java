package com.huwei.gmall0416.llist.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.huwei.gmall0416.bean.BaseAttrInfo;
import com.huwei.gmall0416.bean.BaseAttrValue;
import com.huwei.gmall0416.bean.SkuLsParams;
import com.huwei.gmall0416.bean.SkuLsResult;
import com.huwei.gmall0416.service.ListService;
import com.huwei.gmall0416.service.ManageService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Controller
public class ListController {

    @Reference
    private ListService listService;
    @Reference
    private ManageService manageService;

    @RequestMapping("list.html")
    //@ResponseBody
    public String getList(SkuLsParams skuLsParams,Model model){
        skuLsParams.setPageSize(1);
        //从es中查找数据
        SkuLsResult skuLsResult=listService.search(skuLsParams);
        model.addAttribute("skuLsInfoList",skuLsResult.getSkuLsInfoList());
        // 从结果中取出平台属性值列表
        List<String>  attrValueIdList=skuLsResult.getAttrValueIdList();
        //取出平台属性和平台属性值
        List<BaseAttrInfo> attrList = manageService.getAttrList(attrValueIdList);
        model.addAttribute("attrList",attrList);
        //已选的属性值列表
        List<BaseAttrValue> baseAttrValuesList = new ArrayList<>();
       String urlParam = makeUrlParam(skuLsParams);

        for (Iterator<BaseAttrInfo> iterator  = attrList.iterator(); iterator .hasNext(); ) {
            BaseAttrInfo baseAttrInfo =  iterator .next();
            List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();
            for (BaseAttrValue baseAttrValue: attrValueList) {
                baseAttrValue.setUrlParam(urlParam);
                if (skuLsParams.getValueId() != null && skuLsParams.getValueId().length > 0) {
                    for (String valueId : skuLsParams.getValueId()) {
                        //选中的属性值 和 查询结果的属性值
                        if (valueId.equals(baseAttrValue.getId())) {
                            iterator.remove();
                            // 构造面包屑列表
                            BaseAttrValue baseAttrValueSelected = new BaseAttrValue();
                            baseAttrValueSelected.setValueName(baseAttrInfo.getAttrName()+":"+baseAttrValue.getValueName());
                            // 去除重复数据
                            String makeUrlParam = makeUrlParam(skuLsParams,valueId);
                            baseAttrValueSelected.setUrlParam(makeUrlParam);
                            baseAttrValuesList.add(baseAttrValueSelected);
                        }
                    }
                }
            }
        }
        model.addAttribute("totalPages",skuLsResult.getTotalPages());
        model.addAttribute("pageNo",skuLsParams.getPageNo());
        model.addAttribute("baseAttrValuesList",baseAttrValuesList);
        model.addAttribute("keyword",   skuLsParams.getKeyword());
        model.addAttribute("urlParam",urlParam);
        //return JSON.toJSONString(search);
        return  "list";
    }

    public String makeUrlParam(SkuLsParams skuLsParam,String... excludeValueIds){
        String urlParam="";
        if (skuLsParam.getKeyword()!=null){
            urlParam+="keyword="+skuLsParam.getKeyword();
        }
        if (skuLsParam.getCatalog3Id()!=null) {
            if (urlParam.length() > 0) {
                urlParam += "&";
            }
            urlParam+="catalog3Id="+skuLsParam.getCatalog3Id();
        }
        if (skuLsParam.getValueId()!=null && skuLsParam.getValueId().length>0){
           for (int i=0;i<skuLsParam.getValueId().length;i++){
               String valueId = skuLsParam.getValueId()[i];
               if(excludeValueIds!=null && excludeValueIds.length>0) {
                   String excludeValueId = excludeValueIds[0];
                   if (excludeValueId.equals(valueId)){
                       // 跳出代码，后面的参数则不会继续追加【后续代码不会执行】
                       continue;
                   }
               }
               if (urlParam.length()>0){
                   urlParam+="&";
               }
               urlParam+="valueId="+valueId;
           }
        }
          return urlParam;
    }
}
