package com.huwei.gmall0416.service;

import com.huwei.gmall0416.bean.UserAddress;
import com.huwei.gmall0416.bean.UserInfo;

import java.util.List;

public interface UserInfoService {

    /**
     * 查询用户全部
     * @return
     */
    public List<UserInfo> findAll();

    /**
     * 根据用户id查询订单
     * @param userId 用户id
     * @return  订单信息
     */
    public List<UserAddress> getUserAddressList(String userId);

    /**
     * 用户登录
     * @param userInfo
     * @return
     */
    public UserInfo login(UserInfo userInfo);

    /**
     * 验证用户
     * @param userId
     * @return
     */
    UserInfo verify(String userId);
}
