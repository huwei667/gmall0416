package com.huwei.gmall0416.cart.serviceImpl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.huwei.gmall0416.bean.CartInfo;
import com.huwei.gmall0416.bean.SkuInfo;
import com.huwei.gmall0416.cart.constant.CartConst;
import com.huwei.gmall0416.cart.mapper.CartInfoMapper;
import com.huwei.gmall0416.config.RedisUtil;
import com.huwei.gmall0416.service.CartService;
import com.huwei.gmall0416.service.ManageService;
import com.sun.xml.internal.fastinfoset.tools.FI_DOM_Or_XML_DOM_SAX_SAXEvent;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.util.*;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private CartInfoMapper cartInfoMapper;

    @Reference
    private ManageService manageService;

    @Autowired
    private RedisUtil redisUtil;


    @Override
    public void addToCart(String skuId, String userId, Integer skuNum) {
        //  先查cart中是否
        CartInfo cartInfo = new CartInfo();
        cartInfo.setSkuId(skuId);
        cartInfo.setUserId(userId);
        //查询数据库是否有购物车
        CartInfo cartInfoExist = cartInfoMapper.selectOne(cartInfo);
       if (cartInfoExist!=null){
           //更新商品数量
           cartInfoExist.setSkuNum(cartInfoExist.getSkuNum()+skuNum);
           cartInfoMapper.updateByPrimaryKeySelective(cartInfoExist);
       }else{
            //如果不存在,报错购物车
           SkuInfo skuInfo=manageService.getSkuInfo(skuId);
           CartInfo cartInfo1 = new CartInfo();
           cartInfo1.setSkuId(skuId);
           cartInfo1.setCartPrice(skuInfo.getPrice());
           cartInfo1.setSkuPrice(skuInfo.getPrice());
           cartInfo1.setSkuName(skuInfo.getSkuName());
           cartInfo1.setImgUrl(skuInfo.getSkuDefaultImg());
           cartInfo1.setUserId(userId);
           cartInfo1.setSkuNum(skuNum);
           //插入数据库
           cartInfoMapper.insertSelective(cartInfo1);
           cartInfoExist=cartInfo1;
       }
        //插入redis缓存
        // 构建key user:userid:cart
        String userCartKey = CartConst.USER_KEY_PREFIX+userId+ CartConst.USER_CART_KEY_SUFFIX;
        Jedis jedis = redisUtil.getJedis();
        // 将对象序列化
        String cartJson = JSON.toJSONString(cartInfoExist);

        jedis.hset(userCartKey,skuId,cartJson);
        // 更新购物车过期时间
        String userInfoKey = CartConst.USER_KEY_PREFIX+userId+CartConst.USERINFOKEY_SUFFIX;
        //获取用户登录的过期时间
        Long ttl = jedis.ttl(userInfoKey);
        //更新过期时间
        jedis.expire(userCartKey,ttl.intValue());
        //关闭
        jedis.close();

    }

    @Override
    public List<CartInfo> getCartList(String userId) {
        // 从redis中取得，
        Jedis jedis = redisUtil.getJedis();
        //定义购物车的key
        String userCartKey = CartConst.USER_KEY_PREFIX+userId+CartConst.USER_CART_KEY_SUFFIX;
        //通过key取出所有数据
        List<String> cartJsons = jedis.hvals(userCartKey);
        if (cartJsons!=null&&cartJsons.size()>0){
            List<CartInfo> cartInfoList = new ArrayList<>();
            for (String cartJson : cartJsons) {
                CartInfo cartInfo = JSON.parseObject(cartJson, CartInfo.class);
                cartInfoList.add(cartInfo);
            }
            // 排序
            cartInfoList.sort(new Comparator<CartInfo>() {
                @Override
                public int compare(CartInfo o1, CartInfo o2) {
                    return Long.compare(Long.parseLong(o2.getId()),Long.parseLong(o1.getId()));
                }
            });
            jedis.close();
            return cartInfoList;
        }else{
            // 从数据库中查询，其中cart_price 可能是旧值，所以需要关联sku_info 表信息。
            List<CartInfo> cartInfoList = loadCartCache(userId);
            return cartInfoList;
        }
    }

    @Override
    public List<CartInfo> loadCartCache(String userId) {
        List<CartInfo> cartInfoList = cartInfoMapper.selectCartListWithCurPrice(userId);
        if (cartInfoList==null && cartInfoList.size()==0){
            return null;
        }
        //购物车key
        String userCartKey = CartConst.USER_KEY_PREFIX+userId+CartConst.USER_CART_KEY_SUFFIX;
        Jedis jedis = redisUtil.getJedis();
        Map<String,String> map = new HashMap<>(cartInfoList.size());
        for (CartInfo cartInfo : cartInfoList) {
            String cartJson = JSON.toJSONString(cartInfo);
            // key 都是同一个，值会产生重复覆盖！
            map.put(cartInfo.getSkuId(),cartJson);
        }
        // 将java list - redis hash
        jedis.hmset(userCartKey,map);
        jedis.close();
        return  cartInfoList;

    }

    @Override
    public List<CartInfo> mergeToCartList(List<CartInfo> cartListFromCookie, String userId) {
        //根据用户id查询数据的购物车数据
        List<CartInfo> cartInfoListDB = cartInfoMapper.selectCartListWithCurPrice(userId);
        // 循环开始匹配
        for (CartInfo cartInfoCk : cartListFromCookie) {
            boolean isMatch =false;
            for (CartInfo cartInfoDB : cartInfoListDB) {
                //匹配就这设置价格和修改
                if (cartInfoDB.getSkuId().equals(cartInfoCk.getSkuId())){
                    cartInfoDB.setSkuNum(cartInfoCk.getSkuNum()+cartInfoDB.getSkuNum());
                    cartInfoMapper.updateByPrimaryKeySelective(cartInfoDB);
                    isMatch=true;
                }
            }
            // 数据库中没有购物车，则直接将cookie中购物车添加到数据库
            if (!isMatch){
                cartInfoCk.setUserId(userId);
                cartInfoMapper.insertSelective(cartInfoCk);
            }
        }
        // 从新在数据库中查询并返回数据
        List<CartInfo> cartInfoList = loadCartCache(userId);

        for (CartInfo cartInfo : cartInfoList){
            for (CartInfo info : cartListFromCookie) {
                if (cartInfo.getSkuId().equals(info.getSkuId())){
                    // 只有被勾选的才会进行更改
                    cartInfo.setIsChecked(info.getIsChecked());
                    // 更新redis中的isChecked
                    checkCart(cartInfo.getSkuId(),info.getIsChecked(),userId);
                }
            }
        }
        return cartInfoList;
    }

    @Override
    public void checkCart(String skuId, String isChecked, String userId) {
        // 更新购物车中的isChecked标志
        Jedis jedis = redisUtil.getJedis();
        // 取得购物车中的信息
        String userCartKey = CartConst.USER_KEY_PREFIX+userId+CartConst.USER_CART_KEY_SUFFIX;
        String cartJson=jedis.hget(userCartKey,skuId);
        // 将cartJson 转换成对象
        CartInfo cartInfo = JSON.parseObject(cartJson, CartInfo.class);
          cartInfo.setIsChecked(isChecked);
          String cartCheckdJson= JSON.toJSONString(cartInfo);
        jedis.hset(userCartKey,skuId,cartCheckdJson);
        // 新增到已选中购物车
        String userCheckedKey = CartConst.USER_KEY_PREFIX + userId + CartConst.USER_CHECKED_KEY_SUFFIX;
        if (isChecked.equals("1")){
            jedis.hset(userCheckedKey,skuId,cartCheckdJson);
        }else{
            jedis.hdel(userCheckedKey,skuId);
        }
        jedis.close();
    }

    @Override
    public List<CartInfo> getCartCheckedList(String userId) {
      // 获得redis中的key
        String userCheckedKey = CartConst.USER_KEY_PREFIX + userId + CartConst.USER_CHECKED_KEY_SUFFIX;
        Jedis jedis = redisUtil.getJedis();
        List<String> cartCheckedList = jedis.hvals(userCheckedKey);
        List<CartInfo> newCartList = new ArrayList<>();
        for (String cartJson : cartCheckedList) {
            CartInfo cartInfo = JSON.parseObject(cartJson,CartInfo.class);
            newCartList.add(cartInfo);
        }
        jedis.close();
        return newCartList;
    }
}
