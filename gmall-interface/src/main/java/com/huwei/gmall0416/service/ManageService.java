package com.huwei.gmall0416.service;

import com.huwei.gmall0416.bean.*;

import java.util.List;

/**
 *
 */
public interface ManageService {

    public List<BaseCatalog1> getCatalog1();

    public List<BaseCatalog2> getCatalog2(String catalog1Id);

    public List<BaseCatalog3> getCatalog3(String catalog2Id);
    /**
     * @param catalog3Id
     * @return
     */
    public List<BaseAttrInfo> getAttrList(String catalog3Id);
    //添加三级分类和属性值
    public void saveAttrInfo(BaseAttrInfo baseAttrInfo);
    //编辑
    public BaseAttrInfo getAttrInfo(String attrId);

    public  List<SpuInfo> getSpuInfoList(SpuInfo spuInfo);

    //基本销售属性表
    public  List<BaseSaleAttr> getBaseSaleAttrList();

    public void saveSpuInfo(SpuInfo spuInfo);

    // 根据spuId获取spuImage中的所有图片列表
     List<SpuImage> getSpuImageList(String spuId);

    //根据spuId查询spuSaleAttr表
    List<SpuSaleAttr> getSpuSaleAttrList(String spuId);

    //插入sku信息
    void saveSkuInfo(SkuInfo skuInfo);


    //----------------------前台接口--------------------------
    //根据skuId查询数据
    SkuInfo getSkuInfo(String skuId);
    //根据skuid和spuid查询出销售和销售属性
    List<SpuSaleAttr> selectSpuSaleAttrListCheckBySku(SkuInfo skuInfo);
    //根据spuId查询出销售属性
    public List<SkuSaleAttrValue> getSkuSaleAttrValueListBySpu(String spuId);
    //查询出销售属性和属性值
    public List<BaseAttrInfo> getAttrList(List<String> attrValueIdList);
}
