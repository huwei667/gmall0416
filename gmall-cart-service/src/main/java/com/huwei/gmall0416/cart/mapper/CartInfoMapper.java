package com.huwei.gmall0416.cart.mapper;

import com.huwei.gmall0416.bean.CartInfo;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;


public interface CartInfoMapper extends Mapper<CartInfo>{


  public  List<CartInfo> selectCartListWithCurPrice(String userId);
}
