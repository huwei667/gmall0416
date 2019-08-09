package com.huwei.gmall0416.manage.mapper;

import com.huwei.gmall0416.bean.BaseAttrInfo;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface BaseAttrInfoMapper extends Mapper<BaseAttrInfo> {


    // 根据三级分类id查询属性表
    List<BaseAttrInfo> getBaseAttrInfoListByCatalog3Id(Long catalog3Id);

    //------------------前台--------------
    //根据属性值id查询平台属性和平台属性值
    List<BaseAttrInfo> selectAttrInfoListByIds(@Param("valueIds") String attrValueIds);
}
