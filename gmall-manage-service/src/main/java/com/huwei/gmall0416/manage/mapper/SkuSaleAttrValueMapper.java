package com.huwei.gmall0416.manage.mapper;

import com.huwei.gmall0416.bean.SkuSaleAttrValue;
import tk.mybatis.mapper.common.Mapper;
import java.util.List;
public interface SkuSaleAttrValueMapper extends Mapper<SkuSaleAttrValue> {
    //---------前台接口---------------
    //根据spuId查询出销售属性值
    public List<SkuSaleAttrValue> selectSkuSaleAttrValueListBySpu (String spuId);

}
