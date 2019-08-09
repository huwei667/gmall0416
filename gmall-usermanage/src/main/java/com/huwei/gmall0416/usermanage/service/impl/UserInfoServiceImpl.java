package com.huwei.gmall0416.usermanage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.huwei.gmall0416.bean.UserAddress;
import com.huwei.gmall0416.bean.UserInfo;
import com.huwei.gmall0416.config.RedisUtil;
import com.huwei.gmall0416.service.UserInfoService;
import com.huwei.gmall0416.usermanage.mapper.UserAddressMapper;
import com.huwei.gmall0416.usermanage.mapper.UserInfoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import redis.clients.jedis.Jedis;

import java.util.List;

//@Service spring的注入
@Service   // dubbo注入
public class UserInfoServiceImpl  implements UserInfoService {

    public String userKey_prefix="user:";
    public String userinfoKey_suffix=":info";
    public int    userKey_timeOut=60*60;


     @Autowired
     private UserInfoMapper userInfoMapper;

     @Autowired
     private UserAddressMapper userAddressMapper;

     @Autowired
     private RedisUtil redisUtil;
    @Override
    public List<UserInfo> findAll() {
        return userInfoMapper.selectAll();
    }

    /**
     * @param userId 用户id
     * @return
     */
    @Override
    public List<UserAddress> getUserAddressList(String userId) {
        List<UserAddress> addressList = null;
        UserAddress userAddress = new UserAddress();
        userAddress.setUserId(userId);
        addressList = userAddressMapper.select(userAddress);
        return addressList;
    }

    @Override
    public UserInfo login(UserInfo userInfo) {
      String password= DigestUtils.md5DigestAsHex(userInfo.getPasswd().getBytes());
      //202CB962AC59075B964B07152D234B70
        userInfo.setPasswd(password);
        UserInfo info = userInfoMapper.selectOne(userInfo);
        System.out.println("info-=============="+info);
        if (info!=null){
            // 获得到redis ,将用户存储到redis中
            Jedis jedis = redisUtil.getJedis();
            jedis.setex(userKey_prefix+info.getId()+userinfoKey_suffix,userKey_timeOut, JSON.toJSONString(info));
            jedis.close();
            return info;
        }
        return null;
    }

    @Override
    public UserInfo verify(String userId) {
        Jedis jedis = redisUtil.getJedis();
        //拼key
        String key = userKey_prefix+userId+userinfoKey_suffix;
        //获取key
        String userJson = jedis.get(key);
        jedis.close();
        if (userJson!=null){
            UserInfo userInfo = JSON.parseObject(userJson, UserInfo.class);
            return  userInfo;
        }
        return null;
    }
}
