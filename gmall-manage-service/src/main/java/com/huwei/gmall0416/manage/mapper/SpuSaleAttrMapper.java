package com.huwei.gmall0416.manage.mapper;
import com.huwei.gmall0416.bean.SpuSaleAttr;
import tk.mybatis.mapper.common.Mapper;
import java.util.List;
public interface SpuSaleAttrMapper extends Mapper<SpuSaleAttr> {

    //根据spuId查询spuSaleAttr表
    List<SpuSaleAttr> selectSpuSaleAttrList(String spuId);

    //----------前台接口------
    //根据skuid和spuid查询出销售属性和属性值
    List<SpuSaleAttr> selectSpuSaleAttrListCheckBySku(long skuId,long spuId);

}
