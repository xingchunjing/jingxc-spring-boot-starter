package top.jingxc.server.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.extern.log4j.Log4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletWebRequest;
import top.jingxc.server.aop.OperationLogger;
import top.jingxc.server.client.service.FeignClientService;
import top.jingxc.server.config.ConstantCommon;
import top.jingxc.server.config.ReturnResult;
import top.jingxc.server.config.ReturnResultError;
import top.jingxc.server.config.ReturnResultSuccess;
import top.jingxc.server.service.AppleService;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.ECPublicKey;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Log4j
@Service
public class AppleServiceImpl implements AppleService {

    @Autowired
    private FeignClientService client;

    @Override
    @OperationLogger
    public ReturnResult verifyReceipt(String receipt) {

        Map<String, Object> params = new HashMap<>();
        params.put("receipt-data", receipt);

        Map<String, String> verifyResult = client.postByJsonToMap("https://buy.itunes.apple.com/verifyReceipt", new HashMap<>(), params);
        if (verifyResult == null) {
            // 苹果服务器没有返回验证结果
            return ReturnResultError.builder().code(ConstantCommon.RETURN_CODE_999).msg("订单不存在").data("").build();
        } else {
            // 苹果验证有返回结果
            log.warn("线上，苹果平台返回JSON:" + verifyResult);

            String states = verifyResult.get("status");
            if (ConstantCommon.RECEIPT_RETURN_STATUS_21007.equals(states)) {
                // 是沙盒环境，应沙盒测试，否则执行下面
                verifyResult = client.postByJsonToMap("https://sandbox.itunes.apple.com/verifyReceipt", new HashMap<>(), params);

                log.warn("沙盒环境，苹果平台返回JSON:" + verifyResult);
                states = verifyResult.get("status");
            }
            // 前端所提供的收据是有效的 验证成功
            if (ConstantCommon.RECEIPT_RETURN_STATUS_0.equals(states)) {
                String receiptJson = JSON.toJSONString(verifyResult.get("receipt"));
                JSONObject returnJson = JSON.parseObject(receiptJson);
                String inApp = returnJson.getString("in_app");
                String bundleId = returnJson.getString("bundle_id");
                //TODO 验证包结构数据
                //bundleId是否是app包名
                JSONArray jsonArray = JSONObject.parseArray(inApp);
                for (int i = 0; i < jsonArray.size(); i++) {
                    // 获取订单信息对象
                    JSONObject targetOrder = jsonArray.getJSONObject(i);
                    // 获取产品信息
                    String productId = targetOrder.getString("product_id");
                    // transaction_id交易号
                    String transactionId = targetOrder.getString("transaction_id");
                    if (StringUtils.isNotBlank(productId) && StringUtils.isNotBlank(transactionId)) {
                        //TODO 并且服务端订单数据也是正常状态，修改订单状态，如果是作废、已校验、已发货做其他操作
                    }
                }
            }
        }
        return ReturnResultSuccess.builder().code(ConstantCommon.RETURN_CODE_200).msg("success")
                .data("返回详情").build();
    }

    @Override
    public ReturnResult refundV1(String data) {
        log.warn("苹果退款数据v1:" + data);

        String receipt = JSON.toJSONString(JSON.parseObject(data).get("unified_receipt"));
        JSONObject parseObject = JSON.parseObject(receipt);
        String receiptInfo = JSON.toJSONString(parseObject.get("latest_receipt_info"));
        JSONArray parseArray = JSON.parseArray(receiptInfo);

        for (Object object : parseArray) {
            JSONObject da = JSON.parseObject(JSON.toJSONString(object));
            String transactionId = da.get("transaction_id").toString();//平台订单id
            //TODO 根据订单id修改修改订单状态，以及其他操作
        }
        return ReturnResultSuccess.builder().code(ConstantCommon.RETURN_CODE_200).msg("success").data("")
                .count(parseArray.size()).build();
    }

    @Override
    public ReturnResult refundV2(String jwt) {
        // 拿到 header 中 x5c 数组中第一个
        DecodedJWT decodedJWT = JWT.decode(jwt);
        String header = new String(Base64.getDecoder().decode(decodedJWT.getHeader()));

        String x5cs = JSON.toJSONString(JSON.parseObject(header).get("x5c"));
        String x5c = JSON.toJSONString(JSON.parseArray(x5cs).get(0));

        // 获取公钥
        try {
            PublicKey publicKey = getPublicKeyByX5c(x5c);

            // 验证 token
            Algorithm algorithm = Algorithm.ECDSA256((ECPublicKey) publicKey, null);
            algorithm.verify(decodedJWT);
        } catch (Exception e) {
            e.printStackTrace();
            //TODO 修改返回码状态
            HttpServletResponse response = ((ServletWebRequest) RequestContextHolder.getRequestAttributes()).getResponse();
            response.setStatus(403);
            return ReturnResultError.builder().code(ConstantCommon.RETURN_CODE_999).msg("验证失败").data("").build();

        }
        String payload = new String(Base64.getDecoder().decode(decodedJWT.getPayload()));
        String data = JSON.toJSONString(JSON.parseObject(header).get("data"));

        //TODO 获取数据，和本地数据对比验证，修改订单状态
        return ReturnResultSuccess.builder().code(ConstantCommon.RETURN_CODE_200).msg("验证成功").data("")
                .count(ConstantCommon.RETURN_COUNT_1).build();
    }

    public static PublicKey getPublicKeyByX5c(String x5c) throws CertificateException {
        byte[] x5c0Bytes = Base64.getDecoder().decode(x5c);
        CertificateFactory fact = CertificateFactory.getInstance("X.509");
        X509Certificate cer = (X509Certificate) fact.generateCertificate(new ByteArrayInputStream(x5c0Bytes));
        return cer.getPublicKey();
    }
}
