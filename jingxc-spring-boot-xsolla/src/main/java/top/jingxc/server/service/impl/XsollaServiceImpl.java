package top.jingxc.server.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.jingxc.server.aop.OperationLogger;
import top.jingxc.server.bean.constant.ConstantMeInfo;
import top.jingxc.server.bean.order.OrderMeInfo;
import top.jingxc.server.bean.product.ProductMeInfo;
import top.jingxc.server.bean.user.UserAccountInfo;
import top.jingxc.server.client.service.FeignClientService;
import top.jingxc.server.config.ConstantCommon;
import top.jingxc.server.config.ReturnResult;
import top.jingxc.server.config.ReturnResultError;
import top.jingxc.server.config.ReturnResultSuccess;
import top.jingxc.server.mapper.ConstantMeInfoMapper;
import top.jingxc.server.mapper.OrderMeInfoMapper;
import top.jingxc.server.mapper.ProductMeInfoMapper;
import top.jingxc.server.mapper.UserAccountInfoMapper;
import top.jingxc.server.param.CreateOrderParams;
import top.jingxc.server.param.CreateTokenParams;
import top.jingxc.server.param.RefundOrderParams;
import top.jingxc.server.service.XsollaService;
import top.jingxc.server.util.BeanMapUtils;
import top.jingxc.server.util.OauthSignatureUtil;
import top.jingxc.server.util.RedisCacheUtil;

import java.util.HashMap;
import java.util.Map;

@Service
public class XsollaServiceImpl implements XsollaService {

    @Autowired
    private UserAccountInfoMapper userAccountInfoMapper;
    @Autowired
    private RedisCacheUtil redisCacheUtil;
    @Autowired
    private ConstantMeInfoMapper constantMeInfoMapper;
    @Autowired
    private FeignClientService client;
    @Autowired
    private ProductMeInfoMapper productMeInfoMapper;
    @Autowired
    private OrderMeInfoMapper orderMeInfoMapper;

    @Override
    @OperationLogger
    public ReturnResult createToken(CreateTokenParams params) {

        String userId = params.getUserId();
        String gameId = params.getGameId();
        String channelId = params.getChannelId();
        String email = params.getEmail();
        String currencyType = params.getCurrencyType();

        //验证账号是否存在
        UserAccountInfo accountInfo = UserAccountInfo.builder().userId(userId).build();
        accountInfo = userAccountInfoMapper.selectOne(new QueryWrapper<>(accountInfo));
        if (accountInfo == null) {
            return ReturnResultError.builder().code(ConstantCommon.RETURN_CODE_903).msg("账号不存在").data("")
                    .build();
        }

        String username = accountInfo.getUsername();

        //查询渠道配置信息
        ConstantMeInfo constantInfo = getCacheConstantInfo(gameId, channelId, "xsolla");

        //授权信息
        String xsollaUserId = constantInfo.getUserId();//商户id
        String appId = constantInfo.getAppId();//项目id
        String appKey = constantInfo.getAppKey();//密钥
        String headerBasic = OauthSignatureUtil.headerBasic(xsollaUserId, appKey);

        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", headerBasic);

        //请求体
        Map<String, Object> xsollaParams = new HashMap<>();
        xsollaParams.put("settings", new HashMap<String, Object>() {{
            put("currency", currencyType);
            put("language", "en");//xsolla支付界面语言。可定制化配置
            put("project_id", Integer.parseInt(appId));
        }});
        xsollaParams.put("user", new HashMap<String, Object>() {{
            put("email", new HashMap<String, String>() {{
                put("value", email);
            }});
            put("id", new HashMap<String, String>() {{
                put("value", userId);
            }});
            put("name", new HashMap<String, String>() {{
                put("value", username);
            }});
        }});

        Map<String, String> createToken = client.postByJsonToMap("https://api.xsolla.com/merchant/v2/merchants/" + xsollaUserId + "/token", headers, xsollaParams);

        System.out.println(JSON.toJSONString(createToken));

        //TODO 存储令牌信息，也可以直接和创建订单合并

        return ReturnResultSuccess.builder().code(ConstantCommon.RETURN_CODE_200).msg("令牌创建成功").data(JSON.toJSONString(createToken))
                .count(ConstantCommon.RETURN_COUNT_1).build();
    }

    @Override
    @OperationLogger
    public ReturnResult createOrder(CreateOrderParams params) {

        String userId = params.getUserId();

        //验证账号是否存在
        UserAccountInfo accountInfo = UserAccountInfo.builder().userId(userId).build();
        accountInfo = userAccountInfoMapper.selectOne(new QueryWrapper<>(accountInfo));
        if (accountInfo == null) {
            return ReturnResultError.builder().code(ConstantCommon.RETURN_CODE_903).msg("账号不存在").data("")
                    .build();
        }

        //查询商品信息
        String gameId = params.getGameId();
        String channelId = params.getChannelId();
        String productId = params.getProductId();
        String currencyType = params.getCurrencyType();
        String token = params.getToken();//获取得到的用户令牌，也可以将方法合一
        ProductMeInfo productInfo = getCacheProductInfo(gameId, channelId, productId, currencyType);

        //查询渠道配置信息
        ConstantMeInfo constantInfo = getCacheConstantInfo(gameId, channelId, "xsolla");

        //授权信息
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + token);

        //配置ui显示
        Map<String, Object> ui = new HashMap<>();
        ui.put("desktop", new HashMap<String, Object>() {{//桌面版本的界面设置。
            put("header", new HashMap<String, Object>() {{
                put("close_button", false);//是否在支付中心桌面端显示关闭按钮。该按钮将关闭支付中心并将用户重定向到settings.return_url参数中指定的URL。默认为false。
                put("is_visible", true);//页眉在支付UI上是否可见。
                put("type", "normal");//如何显示页眉。不能：compact（隐藏项目名称和用户ID）或normal（默认）
                put("visible_logo", true);//标题中将显示Logo
                put("visible_name", true);//页眉中是否显示项目名称。
                put("visible_purchase", true);//是否在页眉中显示购买描述(purchase.description.value )
            }});
        }});
        ui.put("mobile", new HashMap<String, Object>() {{
            put("footer", new HashMap<String, Object>() {{
                put("is_visible", true);//是否在移动版本的支付UI中隐藏或显示脚注。
            }});
            put("header", new HashMap<String, Object>() {{
                put("close_button", false);//是否在支付中心移动端显示关闭按钮。该按钮将关闭支付中心并将用户重定向到settings.return_url参数中指定的URL。默认为false。
            }});
        }});
        ui.put("size", "large");
        ui.put("theme", "default");
        ui.put("version", "mobile");//设备的类型。可以是desktop（默认值）或mobile。

        Map<String, Object> xsollaParams = new HashMap<>();
        xsollaParams.put("settings", new HashMap<String, Object>() {{
            put("ui", ui);
            put("return_url", constantInfo.getReturnUrl());
        }});
        xsollaParams.put("sandbox", true);
        xsollaParams.put("quantity", 1);
        xsollaParams.put("custom_parameters", new HashMap<String, Object>() {{//项目特定参数。
            put("character_id", userId);
        }});

        Map<String, String> createOrder = client.postByJsonToMap("https://store.xsolla.com/api/v2/project/" +
                constantInfo.getAppId() + "/payment/item/" + productId, headers, xsollaParams);

        //TODO 存储订单

        System.out.println(JSON.toJSONString(createOrder));
        return ReturnResultSuccess.builder().code(ConstantCommon.RETURN_CODE_200).msg("订单创建成功").data(JSON.toJSONString(createOrder))
                .count(ConstantCommon.RETURN_COUNT_1).build();
    }

    @Override
    @OperationLogger
    public ReturnResult refundOrder(RefundOrderParams params) {

        String userId = params.getUserId();
        String orderId = params.getOrderId();
        String gameId = params.getGameId();
        String channelId = params.getChannelId();
        String transcationId = params.getTranscationId();
        String desc = params.getDesc();

        //查询订单是否存在
        OrderMeInfo orderMeInfo = OrderMeInfo.builder().gameId(gameId).channelId(channelId).orderId(orderId).userId(userId)
                .orderStatus(ConstantCommon.ORDER_STATUS_1).build();
        orderMeInfo = orderMeInfoMapper.selectOne(new QueryWrapper<>(orderMeInfo));
        if (orderMeInfo == null) {
            return ReturnResultError.builder().code(ConstantCommon.RETURN_CODE_903).msg("订单不存在或者不存在/已退款").data("")
                    .build();
        }

        //查询渠道配置信息
        ConstantMeInfo constantInfo = getCacheConstantInfo(gameId, channelId, "xsolla");

        //授权信息
        String xsollaUserId = constantInfo.getUserId();//商户id
        String appKey = constantInfo.getAppKey();//密钥
        String headerBasic = OauthSignatureUtil.headerBasic(xsollaUserId, appKey);

        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", headerBasic);

        Map<String, Object> xsollaParams = new HashMap<>();
        xsollaParams.put("description", "退款理由" + desc);
        xsollaParams.put("email", "18712709017@163.com");//用户邮箱

        Map<String, String> refund = client.postByJsonToMap("https://api.xsolla.com/merchant/v2/merchants/"
                + constantInfo.getUserId() + "/reports/transactions/" + transcationId + "/refund", headers, xsollaParams);
        System.out.println(JSON.toJSONString(refund));
        return ReturnResultSuccess.builder().code(ConstantCommon.RETURN_CODE_200).msg("退款成功").data(refund)
                .count(ConstantCommon.RETURN_COUNT_1).build();
    }

    /**
     * 获取配置缓存
     *
     * @param gameId
     * @param channelId
     * @param platformId
     */
    private ConstantMeInfo getCacheConstantInfo(String gameId, String channelId, String platformId) {
        String redisKey = ConstantCommon.KEY_PREFIX_CONSTANT + gameId + "_" + channelId + "_" + platformId;
        boolean hasKey = redisCacheUtil.hasKey(redisKey);
        ConstantMeInfo constantMeInfo = new ConstantMeInfo();
        if (hasKey) {
            Map<Object, Object> hmget = redisCacheUtil.hmget(redisKey);
            constantMeInfo = BeanMapUtils.mapToEntity(hmget, ConstantMeInfo.class);
        } else {
            ConstantMeInfo meInfo = ConstantMeInfo.builder().gameId(gameId).channelId(channelId).platformId(platformId).build();
            constantMeInfo = constantMeInfoMapper.selectOne(new QueryWrapper<>(meInfo));
            Map<String, Object> hmap = BeanMapUtils.entityToMapObject(constantMeInfo);
            redisCacheUtil.hmset(redisKey, hmap, 24 * 60 * 60L);
        }
        return constantMeInfo;
    }

    /**
     * 获取商品缓存
     *
     * @param gameId
     * @param channelId
     * @param productId
     * @param currencyType
     * @return
     */
    private ProductMeInfo getCacheProductInfo(String gameId, String channelId, String productId, String currencyType) {

        String redisKey = ConstantCommon.KEY_PREFIX_PRODUCT + gameId + "_" + channelId + "_" + productId + "_" + currencyType;
        boolean hasKey = redisCacheUtil.hasKey(redisKey);
        ProductMeInfo productMeInfo = new ProductMeInfo();
        if (hasKey) {
            Map<Object, Object> hmget = redisCacheUtil.hmget(redisKey);
            productMeInfo = BeanMapUtils.mapToEntity(hmget, ProductMeInfo.class);
        } else {
            ProductMeInfo meInfo = ProductMeInfo.builder().gameId(gameId).channelId(channelId).productId(productId).currencyType(currencyType).build();
            productMeInfo = productMeInfoMapper.selectOne(new QueryWrapper<>(meInfo));
            Map<String, Object> hmap = BeanMapUtils.entityToMapObject(productMeInfo);
            redisCacheUtil.hmset(redisKey, hmap, 24 * 60 * 60L);
        }
        return productMeInfo;
    }
}
