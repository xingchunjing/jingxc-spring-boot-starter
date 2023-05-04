package top.jingxc.server.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import lombok.extern.log4j.Log4j;
import org.apache.tomcat.util.codec.binary.Base64;
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
import top.jingxc.server.param.*;
import top.jingxc.server.service.PaypalService;
import top.jingxc.server.util.*;

import javax.security.cert.X509Certificate;
import javax.servlet.http.HttpServletRequest;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.PublicKey;
import java.util.*;
import java.util.zip.CRC32;

@Log4j
@Service
public class PaypalServiceImpl implements PaypalService {

    @Autowired
    private RedisCacheUtil redisCacheUtil;
    @Autowired
    private FeignClientService client;
    @Autowired
    private UserAccountInfoMapper userAccountInfoMapper;
    @Autowired
    private ProductMeInfoMapper productMeInfoMapper;
    @Autowired
    private ConstantMeInfoMapper constantMeInfoMapper;
    @Autowired
    private OrderMeInfoMapper orderMeInfoMapper;

    @Override
    @OperationLogger
    public ReturnResult createOrder(CreateOrderParams params) {

        //验证参数准确性（可省略）
        Map<String, String> entityToMap = BeanMapUtils.entityToMapString(params);
        String signParam = params.getSign();
        boolean check = CheckParamUtils.checkSign(entityToMap, signParam, ConstantCommon.KEY_EXTRA_CLIENT);

        if (!check) {
            return ReturnResultError.builder().code(ConstantCommon.RETURN_CODE_902).msg("sign校验失败").data("")
                    .build();
        }

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
        ProductMeInfo productInfo = getCacheProductInfo(gameId, channelId, productId, currencyType);

        //查询渠道配置信息
        ConstantMeInfo constantInfo = getCacheConstantInfo(gameId, channelId, "paypal");

        //授权信息
        String appKey = constantInfo.getAppKey();
        String appSecret = constantInfo.getAppSecret();
        String headerBasic = OauthSignatureUtil.headerBasic(appKey, appSecret);

        //设置PayPal-Request-Id，这个之前没有也能正常使用，最新的API里面有就加上了
        String paypalRequestId = UUID.randomUUID().toString();

        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", headerBasic);
        headers.put("PayPal-Request-Id", paypalRequestId);

        Map<String, Object> paypalParams = new HashMap<>();

        //封装订单purchase_units
        Random random = new Random();
        String orderId = System.currentTimeMillis() + random.nextInt(10000) + "";
        // 订单号边长添加用户ID标识
        String finalOrderId = gameId + '-' + userId + '-' + orderId;
        Map<String, Object> units = new HashMap<>();
        units.put("reference_id", finalOrderId);
        Map<String, Object> amount = new HashMap<String, Object>() {{
            put("value", productInfo.getAmount());
            put("currency_code", currencyType);
        }};
        Map<String, Object> unitsAmount = new HashMap<>(amount);
        amount.put("breakdown", new HashMap<String, Object>() {{
            put("item_total", unitsAmount);
        }});
        units.put("amount", amount);
        units.put("custom_id", userId);
        units.put("description", productInfo.getProductDesc());
        units.put("invoice_id", finalOrderId);

        units.put("items", new ArrayList<Map<String, Object>>() {{
            add(new HashMap<String, Object>() {{
                    put("name", productInfo.getProductName());
                    put("quantity", 1);
                    put("description", productInfo.getProductDesc());
                    put("unit_amount", unitsAmount);
                }}
            );
        }});
        paypalParams.put("purchase_units", new ArrayList<Map<String, Object>>() {{
            add(units);
        }});
        paypalParams.put("intent", "CAPTURE");
        //封装experience_context,application_context字短已经弃用，请查看官方文档
        // Please specify this field in the experience_context object instead of the application_context object.
        Map<String, Object> experienceContext = new HashMap<>();
        //该标签会覆盖PayPal网站上paypal账户中的公司名称
        experienceContext.put("brand_name", "JINGXC");
        /**
         * LOGIN。当客户单击PayPal Checkout时，客户将被重定向到页面以登录PayPal并批准付款。
         * BILLING。当客户单击PayPal Checkout时，客户将被重定向到一个页面，以输入信用卡或借记卡以及完成购买所需的其他相关账单信息
         * NO_PREFERENCE。当客户单击“ PayPal Checkout”时，将根据其先前的交互方式将其重定向到页面以登录PayPal并批准付款，
         * 或重定向至页面以输入信用卡或借记卡以及完成购买所需的其他相关账单信息使用PayPal。
         * 默认值：NO_PREFERENCE
         */
        experienceContext.put("landing_page", "NO_PREFERENCE");
        /**
         * GET_FROM_FILE。使用贝宝网站上客户提供的送货地址。
         * NO_SHIPPING。从PayPal网站编辑送货地址。推荐用于数字商品
         * SET_PROVIDED_ADDRESS。使用商家提供的地址。客户无法在PayPal网站上更改此地址
         */
        experienceContext.put("shipping_preference", "NO_SHIPPING");
        /**
         * CONTINUE。将客户重定向到PayPal付款页面后，将出现“ 继续”按钮。当结帐流程启动时最终金额未知时，请使用此选项，
         * 并且您想将客户重定向到商家页面而不处理付款。
         * PAY_NOW。将客户重定向到PayPal付款页面后，出现“ 立即付款”按钮。当启动结帐时知道最终金额并且您要在客户单击“
         * 立即付款”时立即处理付款时，请使用此选项。
         */
        experienceContext.put("user_action", "PAY_NOW");
        /**
         *UNRESTRICTED:接受客户的任何类型的付款。
         *IMMEDIATE_PAYMENT_REQUIRED:只接受客户的即时付款。例如，信用卡、PayPal余额或即时ACH。
         * 确保在捕获时，付款没有挂起状态。
         */
        experienceContext.put("payment_method_preference", "IMMEDIATE_PAYMENT_REQUIRED");
        //客户批准付款后客户被重定向的URL。
        experienceContext.put("return_url", constantInfo.getReturnUrl());
        //客户取消付款后客户被重定向的URL。
        experienceContext.put("cancel_url", constantInfo.getCancelUrl());
        paypalParams.put("payment_source", new HashMap<String, Object>() {{
            put("paypal", new HashMap<String, Object>() {{
                put("experience_context", experienceContext);
            }});
        }});
        System.out.println(JSON.toJSONString(paypalParams));
        Map<String, String> creadeOrder = client.postByJsonToMap("https://api-m.sandbox.paypal.com/v2/checkout/orders", headers,
                paypalParams);

        String links = JSON.toJSONString(creadeOrder.get("links"));
        String platformOrderId = creadeOrder.get("id");
        String transactionUrl = "";
        List<Map> parseArray = JSON.parseArray(links, Map.class);
        for (Map<String, String> link : parseArray) {
            String href = link.get("href");
            String rel = link.get("rel");
            System.out.println("ref:" + rel + "," + "href:" + href);
            if (rel.equals("payer-action")) {
                transactionUrl = href;
            }
        }
        Map<String, String> result = new HashMap<>();
        result.put("transactionUrl", transactionUrl);
        result.put("platformOrderId", platformOrderId);

        OrderMeInfo orderMeInfo = OrderMeInfo.builder().id(IdWorker.getIdStr()).gameId(gameId).channelId(channelId)
                .platformId("paypal").userId(userId).productId(productId).amount(productInfo.getAmount())
                .currencyType(currencyType).orderStatus(ConstantCommon.ORDER_STATUS_0).requestId(paypalRequestId)
                .orderId(finalOrderId).platformOrderId(platformOrderId).build();
        orderMeInfoMapper.insert(orderMeInfo);

        return ReturnResultSuccess.builder().code(ConstantCommon.RETURN_CODE_200).msg("订单创建成功").data(result)
                .count(ConstantCommon.RETURN_COUNT_1).build();
    }

    @Override
    @OperationLogger
    public ReturnResult captureOrder(CaptureOrderParams params) {

        String platformOrderId = params.getPlatformOrderId();
        String gameId = params.getGameId();
        String channelId = params.getChannelId();

        OrderMeInfo orderMeInfo = OrderMeInfo.builder().platformOrderId(platformOrderId).build();
        orderMeInfo = orderMeInfoMapper.selectOne(new QueryWrapper<>(orderMeInfo));
        if (orderMeInfo == null) {
            return ReturnResultError.builder().code(ConstantCommon.RETURN_CODE_999).msg("未查询到该订单").data("")
                    .build();
        }

        //查询渠道配置信息
        ConstantMeInfo constantInfo = getCacheConstantInfo(gameId, channelId, "paypal");

        //授权信息
        String appKey = constantInfo.getAppKey();
        String appSecret = constantInfo.getAppSecret();
        String headerBasic = OauthSignatureUtil.headerBasic(appKey, appSecret);

        //设置PayPal-Request-Id，这个之前没有也能正常使用，最新的API里面有就加上了
        String paypalRequestId = UUID.randomUUID().toString();

        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", headerBasic);
        headers.put("PayPal-Request-Id", paypalRequestId);
        Map<String, String> captureOrder = client.postByJsonToMap("https://api-m.sandbox.paypal.com/v2/checkout/orders/" + platformOrderId + "/capture", headers, new HashMap<>());
        System.out.println(captureOrder);
        String status = captureOrder.get("status");
        if ("COMPLETED".equals(status)) {
            //订单捕获完成付款
            //获取付款id
            String purchaseUnits = JSON.toJSONString(captureOrder.get("purchase_units"));
            String purchaseUnit = JSON.toJSONString(JSON.parseArray(purchaseUnits).get(0));
            String payments = JSON.toJSONString(JSON.parseObject(purchaseUnit).get("payments"));
            String captures = JSON.toJSONString(JSON.parseObject(payments).get("captures"));
            String capture = JSON.toJSONString(JSON.parseArray(captures).get(0));
            String paymentId = (String) JSON.parseObject(capture).get("id");

            orderMeInfo.setPaymentId(paymentId);
            orderMeInfo.setOrderStatus(ConstantCommon.ORDER_STATUS_1);
            orderMeInfoMapper.updateById(orderMeInfo);
            //TODO 发货处理
        }

        return ReturnResultSuccess.builder().code(ConstantCommon.RETURN_CODE_200).msg("订单捕获成功").data(platformOrderId).count(ConstantCommon.RETURN_COUNT_1).build();

    }

    @Override
    @OperationLogger
    public ReturnResult checkOrder(CheckOrderParams params) {

        String platformOrderId = params.getPlatformOrderId();
        String gameId = params.getGameId();
        String channelId = params.getChannelId();
        //查询渠道配置信息
        ConstantMeInfo constantInfo = getCacheConstantInfo(gameId, channelId, "paypal");

        //授权信息
        String appKey = constantInfo.getAppKey();
        String appSecret = constantInfo.getAppSecret();
        String headerBasic = OauthSignatureUtil.headerBasic(appKey, appSecret);

        //设置PayPal-Request-Id，这个之前没有也能正常使用，最新的API里面有就加上了
        String paypalRequestId = UUID.randomUUID().toString();

        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", headerBasic);
        headers.put("PayPal-Request-Id", paypalRequestId);
        Map<String, String> captureOrder = client.getToMap("https://api-m.sandbox.paypal.com/v2/checkout/orders/" + platformOrderId, headers, new HashMap<>());
        System.out.println(captureOrder);

        return ReturnResultSuccess.builder().code(ConstantCommon.RETURN_CODE_200).msg("查看订单状态成功").data(platformOrderId).count(ConstantCommon.RETURN_COUNT_1).build();

    }

    @Override
    @OperationLogger
    public ReturnResult checkCapture(CheckCaptureParams params) {
        String paymentId = params.getPaymentId();
        String gameId = params.getGameId();
        String channelId = params.getChannelId();
        //查询渠道配置信息
        ConstantMeInfo constantInfo = getCacheConstantInfo(gameId, channelId, "paypal");

        //授权信息
        String appKey = constantInfo.getAppKey();
        String appSecret = constantInfo.getAppSecret();
        String headerBasic = OauthSignatureUtil.headerBasic(appKey, appSecret);

        //设置PayPal-Request-Id，这个之前没有也能正常使用，最新的API里面有就加上了
        String paypalRequestId = UUID.randomUUID().toString();

        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", headerBasic);
        headers.put("PayPal-Request-Id", paypalRequestId);
        Map<String, String> captureOrder = client.getToMap("https://api-m.sandbox.paypal.com/v2/payments/captures/" + paymentId, headers, new HashMap<>());
        System.out.println(captureOrder);

        return ReturnResultSuccess.builder().code(ConstantCommon.RETURN_CODE_200).msg("查看订单状态成功").data(paymentId).count(ConstantCommon.RETURN_COUNT_1).build();

    }

    @Override
    @OperationLogger
    public ReturnResult refundCapture(RefundCaptureParams params) {

        String gameId = params.getGameId();
        String channelId = params.getChannelId();
        String orderId = params.getOrderId();
        String noteToPayer = params.getNoteToPayer();
        String userId = params.getUserId();

        //TODO 校验用户与订单是否匹配
        OrderMeInfo orderMeInfo = OrderMeInfo.builder().orderId(orderId).gameId(gameId).userId(userId)
                .orderStatus(ConstantCommon.ORDER_STATUS_1).build();
        orderMeInfo = orderMeInfoMapper.selectOne(new QueryWrapper<>(orderMeInfo));
        if (orderMeInfo == null) {
            return ReturnResultError.builder().code(ConstantCommon.RETURN_CODE_999).msg("未查询到该订单").data("")
                    .build();
        }

        //查询渠道配置信息
        ConstantMeInfo constantInfo = getCacheConstantInfo(gameId, channelId, "paypal");

        //授权信息
        String appKey = constantInfo.getAppKey();
        String appSecret = constantInfo.getAppSecret();
        String headerBasic = OauthSignatureUtil.headerBasic(appKey, appSecret);

        //设置PayPal-Request-Id，这个之前没有也能正常使用，最新的API里面有就加上了
        String paypalRequestId = UUID.randomUUID().toString();

        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", headerBasic);
        headers.put("PayPal-Request-Id", paypalRequestId);
        headers.put("Prefer", "return=representation");

        Map<String, Object> paypalParams = new HashMap<>();
        paypalParams.put("invoice_id", orderId + "-01");
        paypalParams.put("note_to_payer", "退款原因:" + noteToPayer);

        /**
         * Array of objects [ 0 .. 1 ] items
         * An array of various fees, commissions, tips, or donations.
         * This field is only applicable to merchants that been enabled for PayPal Commerce Platform
         * for Marketplaces and Platforms capability.
         */
        Map<String, Object> platformFees = new HashMap<>();
        platformFees.put("amount", new HashMap<String, Object>() {{
            put("value", 1);//真实费用
            put("currency_code", "USD");
        }});

        paypalParams.put("payment_instruction", new HashMap<String, Object>() {{
            put("platform_fees", new ArrayList<Map<String, Object>>() {{
                add(platformFees);
            }});
        }});
        paypalParams.put("amount", new HashMap<String, Object>() {{
            put("value", 10);//真实费用
            put("currency_code", "USD");
        }});

        Map<String, String> refundOrder = client.postByJsonToMap("https://api-m.sandbox.paypal.com/v2/payments/captures/" + orderMeInfo.getPaymentId() + "/refund", headers, new HashMap<>());
        System.out.println(refundOrder);
        String id = refundOrder.get("id");
        orderMeInfo.setRefundId(id);
        orderMeInfoMapper.updateById(orderMeInfo);
        return ReturnResultSuccess.builder().code(ConstantCommon.RETURN_CODE_200).msg("退款成功").data(orderMeInfo.getPaymentId()).count(ConstantCommon.RETURN_COUNT_1).build();

    }

    @Override
    public void webhooks(HttpServletRequest req, String body) {
        String transmissionTime = req.getHeader("paypal-transmission-time");
        String authVersion = req.getHeader("paypal-auth-version");
        String certUrl = req.getHeader("paypal-cert-url");
        String authAlgo = req.getHeader("paypal-auth-algo");
        String transmissionSig = req.getHeader("paypal-transmission-sig");
        String transmissionId = req.getHeader("paypal-transmission-id");

        log.warn("paypal-transmission-time" + transmissionTime);
        log.warn("paypal-auth-version" + authVersion);
        log.warn("paypal-cert-url" + certUrl);
        log.warn("paypal-auth-algo" + authAlgo);
        log.warn("paypal-transmission-sig" + transmissionSig);
        log.warn("paypal-transmission-id" + transmissionId);

        // 校验数据信息
        X509Certificate cert;
        PublicKey publicKey = null;
        try {
            URL url = new URL(certUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            // 设置超时间为30秒
            conn.setConnectTimeout(30 * 1000);
            // 防止屏蔽程序抓取而返回403错误
            conn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
            // 得到输入流
            InputStream inputStream = conn.getInputStream();

            cert = X509Certificate.getInstance(inputStream);
            publicKey = cert.getPublicKey();
        } catch (Exception e) {
            log.error("获取公钥信息失败", e);
        }

        String publicKeyString = Base64.encodeBase64String(publicKey.getEncoded());

        log.warn("公钥信息：" + publicKeyString);
        log.warn("请求参数信息：" + body);
        log.warn("验签数据：" + transmissionSig);

        CRC32 crc32 = new CRC32();
        crc32.update(body.getBytes());
        long value = crc32.getValue();
        String webhookId = "249234092-432";
        String a = transmissionId + "|" + transmissionTime + "|" + webhookId + "|" + value;
        log.warn("验签内容：" + a);
        boolean doCheck = RSA.doCheck(a, transmissionSig, publicKeyString, "RSA256", true);

        if (doCheck) {
            log.warn("校验数据通过，处理订单请求");
        }
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
