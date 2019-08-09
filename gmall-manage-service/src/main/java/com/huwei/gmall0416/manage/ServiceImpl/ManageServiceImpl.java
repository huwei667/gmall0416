package com.huwei.gmall0416.manage.ServiceImpl;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.huwei.gmall0416.bean.*;
import com.huwei.gmall0416.config.RedisUtil;
import com.huwei.gmall0416.manage.constant.ManageConst;
import com.huwei.gmall0416.manage.mapper.*;
import com.huwei.gmall0416.service.ManageService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;


import java.util.List;

@Service
public class ManageServiceImpl implements ManageService {

    @Autowired
    BaseAttrInfoMapper baseAttrInfoMapper;

    @Autowired
    BaseAttrValueMapper baseAttrValueMapper;

    @Autowired
    BaseCatalog1Mapper baseCatalog1Mapper;

    @Autowired
    BaseCatalog2Mapper baseCatalog2Mapper;

    @Autowired
    BaseCatalog3Mapper baseCatalog3Mapper;

    @Autowired
    SpuInfoMapper spuInfoMapper;

    @Autowired
    BaseSaleAttrMapper baseSaleAttrMapper;

    @Autowired
    private SpuImageMapper spuImageMapper;

    @Autowired
    private  SpuSaleAttrMapper spuSaleAttrMapper;

    @Autowired
    private  SpuSaleAttrValueMapper spuSaleAttrValueMapper;
    @Autowired
    private SkuInfoMapper skuInfoMapper;
    @Autowired
    private SkuImageMapper skuImageMapper;
    @Autowired
    private SkuAttrValueMapper skuAttrValueMapper;
    @Autowired
    private SkuSaleAttrValueMapper skuSaleAttrValueMapper;

    @Autowired
    private RedisUtil redisUtil;
    @Override
    public List<BaseCatalog1> getCatalog1() {
        List<BaseCatalog1> baseCatalog1List = baseCatalog1Mapper.selectAll();
        return baseCatalog1List;
    }

    @Override
    public List<BaseCatalog2> getCatalog2(String catalog1Id) {
        BaseCatalog2 baseCatalog2=new BaseCatalog2();
        baseCatalog2.setCatalog1Id(catalog1Id);
        List<BaseCatalog2> baseCatalog2List = baseCatalog2Mapper.select(baseCatalog2);
        return baseCatalog2List;

    }
    @Override
    public List<BaseCatalog3> getCatalog3(String catalog2Id) {
        BaseCatalog3 baseCatalog3=new BaseCatalog3();
        baseCatalog3.setCatalog2Id(catalog2Id);
        List<BaseCatalog3> baseCatalog3List = baseCatalog3Mapper.select(baseCatalog3);
        return baseCatalog3List;
    }
    //
    @Override
    public List<BaseAttrInfo> getAttrList(String catalog3Id) {
       /* BaseAttrInfo baseAttrInfo = new BaseAttrInfo();
        baseAttrInfo.setCatalog3Id(catalog3Id);
        List<BaseAttrInfo> baseAttrInfoList = baseAttrInfoMapper.select(baseAttrInfo);*/
        return baseAttrInfoMapper.getBaseAttrInfoListByCatalog3Id(Long.parseLong(catalog3Id));
    }
    /**
     * 插入三级分类和属性值
     * @param baseAttrInfo
     */
    @Override
    public void saveAttrInfo(BaseAttrInfo baseAttrInfo) {
         //如果有主键就进行更新，如果没有就插入
         if (baseAttrInfo.getId()!=null&&baseAttrInfo.getId().length()>0){
             //根据主键更新  id主键
             baseAttrInfoMapper.updateByPrimaryKey(baseAttrInfo);
         }else{
             //防止主键被赋上一个空字符串
             if (baseAttrInfo.getId().length()==0) {
                 baseAttrInfo.setId(null);
             }
             //先插入数属性表 ,获取当前插入行的id
             baseAttrInfoMapper.insertSelective(baseAttrInfo);
         }
            //把原属性值全部清空
         BaseAttrValue baseAttrValue4Del = new BaseAttrValue();
         baseAttrValue4Del.setAttrId(baseAttrInfo.getId());
         //把修改的值删除
        int delete = baseAttrValueMapper.delete(baseAttrValue4Del);
        System.out.println("id==="+baseAttrInfo.getId()+"delete=========="+delete);
        //在插入属性值表
        if(baseAttrInfo.getAttrValueList()!=null&&baseAttrInfo.getAttrValueList().size()>0) {
            for (BaseAttrValue attrValue :baseAttrInfo.getAttrValueList()) {
                //防止主键被赋上一个空字符串
                if(attrValue.getId().length()==0){
                    attrValue.setId(null);
                }
                attrValue.setAttrId(baseAttrInfo.getId());
                baseAttrValueMapper.insertSelective(attrValue);
            }
        }
    }
    @Override
    //编辑
    public BaseAttrInfo getAttrInfo(String attrId) {
        // 创建属性对象
        BaseAttrInfo attrInfo = baseAttrInfoMapper.selectByPrimaryKey(attrId);
        System.out.println("ID==="+attrId+"attrID=="+attrInfo.getId());
        // 创建属性值对象
        BaseAttrValue baseAttrValue = new BaseAttrValue();
        // 根据attrId字段查询对象
        baseAttrValue.setAttrId(attrInfo.getId());
        List<BaseAttrValue> attrValueList = baseAttrValueMapper.select(baseAttrValue);
        // 给属性对象中的属性值集合赋值
        attrInfo.setAttrValueList(attrValueList);
        // 将属性对象返回
        return attrInfo;
    }

    @Override
    public List<SpuInfo> getSpuInfoList(SpuInfo spuInfo) {

        List<SpuInfo> spuInfoList = spuInfoMapper.select(spuInfo);
        return  spuInfoList;
    }
    @Override
    public List<BaseSaleAttr> getBaseSaleAttrList() {
        return baseSaleAttrMapper.selectAll();
    }
    @Override
    public void saveSpuInfo(SpuInfo spuInfo) {
        // 获取spuInfo， null ==""
        if (spuInfo.getId()==null || spuInfo.getId().length()==0){
            // 插入数据，将Id设置为null
            spuInfo.setId(null);
            spuInfoMapper.insertSelective(spuInfo);
        }else{
            spuInfoMapper.updateByPrimaryKey(spuInfo);
        }
        //  插入SpuImgae之前，根据spuId 将原始数据进行删除。delete from spuImge where spuId = ?  spuId:spuInfo.id
        SpuImage spuImage = new SpuImage();
        spuImage.setSpuId(spuInfo.getId());
        spuImageMapper.delete(spuImage);

        // 插入数据在那？spuInfo.getSpuImageList();
        List<SpuImage> spuImageList=spuInfo.getSpuImageList();
        if (spuImageList!=null && spuImageList.size()>0){
            for (SpuImage image : spuImageList) {
                // image 的id设置为null == "";
                if (image.getId()!=null&&image.getId().length()==0){
                    image.setId(null);
                }
                // image:中有spuId
                image.setSpuId(spuInfo.getId());
                // 插入数据
                spuImageMapper.insertSelective(image);
            }
        }
        // 添加销售属性名称，以及销售属性值
        SpuSaleAttr spuSaleAttr = new SpuSaleAttr();
        spuSaleAttr.setSpuId(spuInfo.getId());
        spuSaleAttrMapper.delete(spuSaleAttr);

        SpuSaleAttrValue spuSaleAttrValue = new SpuSaleAttrValue();
        spuSaleAttrValue.setSpuId(spuInfo.getId());
        spuSaleAttrValueMapper.delete(spuSaleAttrValue);

        // 插入数据
        List<SpuSaleAttr> spuSaleAttrList = spuInfo.getSpuSaleAttrList();
        if (spuSaleAttrList!=null && spuImageList.size()>0){
            for (SpuSaleAttr saleAttr : spuSaleAttrList) {
                if (saleAttr.getId()!=null &&saleAttr.getId().length()==0){
                    saleAttr.setId(null);
                }
                saleAttr.setSpuId(spuInfo.getId());
                spuSaleAttrMapper.insertSelective(saleAttr);
                // 插入属性值
                List<SpuSaleAttrValue> spuSaleAttrValueList = saleAttr.getSpuSaleAttrValueList();
                for (SpuSaleAttrValue saleAttrValue : spuSaleAttrValueList) {
                    if (saleAttrValue.getId()!=null && saleAttrValue.getId().length()==0){
                        saleAttrValue.setId(null);
                    }
                    saleAttrValue.setSpuId(spuInfo.getId());
                    spuSaleAttrValueMapper.insertSelective(saleAttrValue);
                }
            }
        }
    }
    @Override
    public List<SpuImage> getSpuImageList(String spuId) {
        SpuImage spuImage = new SpuImage();
        spuImage.setSpuId(spuId);
        return spuImageMapper.select(spuImage);
    }
    @Override
    public List<SpuSaleAttr> getSpuSaleAttrList(String spuId) {
        return spuSaleAttrMapper.selectSpuSaleAttrList(spuId);
    }
    //插入sku信息
    @Transactional
    @Override
    public void saveSkuInfo(SkuInfo skuInfo) {
        //skuInfo表
        //防止主键不为空
        if(skuInfo.getId()==null||skuInfo.getId().length()==0){
            skuInfo.setId(null);
            skuInfoMapper.insertSelective(skuInfo);
        }else {
            //不为空就修改数据
            skuInfoMapper.updateByPrimaryKeySelective(skuInfo);
        }
        //skuImage表  删除以前修改图片数据
        Example example=new Example(SkuImage.class);
        example.createCriteria().andEqualTo("skuId",skuInfo.getId());
        skuImageMapper.deleteByExample(example);
        //取出所有Img
        List<SkuImage> skuImageList = skuInfo.getSkuImageList();
        for (SkuImage skuImage : skuImageList) {
            //取出skuinfo表的插入的最新Id
            skuImage.setSkuId(skuInfo.getId());
            if(skuImage.getId()!=null&&skuImage.getId().length()==0) {
                skuImage.setId(null);
            }
            skuImageMapper.insertSelective(skuImage);
        }
        //删除旧的sku平台属性
        Example skuAttrValueExample=new Example(SkuAttrValue.class);
        skuAttrValueExample.createCriteria().andEqualTo("skuId",skuInfo.getId());
         skuAttrValueMapper.deleteByExample(skuAttrValueExample);

         //取出sku平台属性
        List<SkuAttrValue> skuAttrValueList = skuInfo.getSkuAttrValueList();
        for (SkuAttrValue skuAttrValue : skuAttrValueList) {
            skuAttrValue.setSkuId(skuInfo.getId());
            if(skuAttrValue.getId()!=null&&skuAttrValue.getId().length()==0) {
                skuAttrValue.setId(null);
            }
            skuAttrValueMapper.insertSelective(skuAttrValue);
         }
        //删除旧的的销售属性值
        Example skuSaleAttrValueExample=new Example(SkuSaleAttrValue.class);
        skuSaleAttrValueExample.createCriteria().andEqualTo("skuId",skuInfo.getId());
        skuSaleAttrValueMapper.deleteByExample(skuSaleAttrValueExample);

       //取出销售属性值
        List<SkuSaleAttrValue> skuSaleAttrValueList = skuInfo.getSkuSaleAttrValueList();
        for (SkuSaleAttrValue skuSaleAttrValue : skuSaleAttrValueList){
             skuSaleAttrValue.setSkuId(skuInfo.getId());
            //防止主键不为空插入数据异常
            skuSaleAttrValue.setId(null);
            skuSaleAttrValueMapper.insertSelective(skuSaleAttrValue);
        }
    }
  //-------------------------前台接口----------
    @Override
    public SkuInfo getSkuInfo(String skuId) {
        SkuInfo skuInfo =null;
        try{
            Jedis jedis=redisUtil.getJedis();
            // 定义key
            String skuInfoKey = ManageConst.SKUKEY_PREFIX+skuId+ManageConst.SKUKEY_SUFFIX; //key= sku:skuId:info
            String skuJson = jedis.get(skuInfoKey);
            //有数据
            if (skuJson==null || skuJson.length()==0){
                // 定义key user:userId::lock
                String skuLockKey=ManageConst.SKUKEY_PREFIX+skuId+ManageConst.SKULOCK_SUFFIX;
                // 生成锁
                String lockKey=jedis.set(skuLockKey, "OK", "NX", "PX", ManageConst.SKULOCK_EXPIRE_PX);
                if ("OK".equals(lockKey)){
                    skuInfo= getSkuInfoDB(skuId);
                    //参数: KEY,过期时间,value
                    String skuRedisStr = JSON.toJSONString(skuInfo);
                    jedis.setex(skuInfoKey,ManageConst.SKUKEY_TIMEOUT,skuRedisStr);
                    jedis.close();
                    return skuInfo;
                }else {
                    System.out.println("等待！");
                    // 等待
                    Thread.sleep(1000);
                    // 调用自己
                    return getSkuInfo(skuId);
                }
            }else{
                // 将数据转换成对象
                skuInfo = JSON.parseObject(skuJson,SkuInfo.class);
                jedis.close();
                System.out.println("没走数据库");
                return  skuInfo;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return getSkuInfoDB(skuId);
   }

   //重构查询数据库方法
    private SkuInfo getSkuInfoDB(String skuId) {
        System.out.println("走数据库了");
        SkuInfo skuInfo=skuInfoMapper.selectByPrimaryKey(skuId);
        String jsonString = JSON.toJSONString(skuInfo);
        SkuImage skuImage = new SkuImage();
        skuImage.setSkuId(skuId);
        List<SkuImage> skuImageList = skuImageMapper.select(skuImage);
        skuInfo.setSkuImageList(skuImageList);
        //--------------es保存要用的的数据----
        SkuAttrValue skuAttrValue=new SkuAttrValue();
        skuAttrValue.setSkuId(skuId);
        List<SkuAttrValue> skuAttrValues= skuAttrValueMapper.select(skuAttrValue);
        skuInfo.setSkuAttrValueList(skuAttrValues);
        return skuInfo;
    }

    @Override
    public List<SpuSaleAttr> selectSpuSaleAttrListCheckBySku(SkuInfo skuInfo) {
        List<SpuSaleAttr> spuSaleAttrs = spuSaleAttrMapper.selectSpuSaleAttrListCheckBySku(Long.parseLong(skuInfo.getId()), Long.parseLong(skuInfo.getSpuId()));
        return spuSaleAttrs;
    }
    
    @Override
    public List<SkuSaleAttrValue> getSkuSaleAttrValueListBySpu(String spuId) {
        List<SkuSaleAttrValue> skuSaleAttrValueList = skuSaleAttrValueMapper.selectSkuSaleAttrValueListBySpu(spuId);
        return skuSaleAttrValueList;
    }

    @Override
    public List<BaseAttrInfo> getAttrList(List<String> attrValueIdList) {
        String attrValueIds = StringUtils.join(attrValueIdList.toArray(), ",");
        System.out.println("attValueIds=="+attrValueIds);
        List<BaseAttrInfo> baseAttrInfoList = baseAttrInfoMapper.selectAttrInfoListByIds(attrValueIds);
        return baseAttrInfoList;
    }
}
