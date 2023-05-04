package top.jingxc.server.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.androidpublisher.AndroidPublisher;
import com.google.api.services.androidpublisher.AndroidPublisherScopes;
import com.google.api.services.androidpublisher.model.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.jingxc.server.client.service.FeignClientService;
import top.jingxc.server.service.GooglePayService;
import top.jingxc.server.util.RSA;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLDecoder;
import java.security.GeneralSecurityException;
import java.util.*;

@Service
public class GooglePayServiceImpl implements GooglePayService {

    @Autowired
    private FeignClientService client;

    private static final java.io.File DATA_STORE_DIR =
            new java.io.File(System.getProperty("user.home"), ".googlepay/pay_sample");

    public static Credential authorizeClient() throws GeneralSecurityException, IOException {

        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
        HttpTransport transport = GoogleNetHttpTransport.newTrustedTransport();
        //InputStreamReader isr = new InputStreamReader(IOUtils.toInputStream(clientSecretsJson, "UTF-8"));
        InputStreamReader isr = new InputStreamReader(GooglePayServiceImpl.class.getClassLoader().getResourceAsStream("./client_secrets.json"));
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(jsonFactory, isr);
        FileDataStoreFactory dataStoreFactory = new FileDataStoreFactory(DATA_STORE_DIR);
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                transport, jsonFactory, clientSecrets,
                Collections.singleton(AndroidPublisherScopes.ANDROIDPUBLISHER))

                .build();
        LocalServerReceiver localServerReceiver = new LocalServerReceiver.Builder().setHost("localhost").setPort(8092).setCallbackPath("/jingxc/google/google-callback").build();
        return new AuthorizationCodeInstalledApp(flow, localServerReceiver).authorize("user");

    }

    public static GoogleCredential authorizeServer() throws GeneralSecurityException, IOException {

        HttpTransport transport = GoogleNetHttpTransport.newTrustedTransport();
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
        InputStream in = GooglePayServiceImpl.class.getClassLoader().getResourceAsStream("./server_secrets.json");

        GoogleCredential readJsonFile = GoogleCredential.fromStream(in, transport, jsonFactory)
                .createScoped(Collections.singleton(AndroidPublisherScopes.ANDROIDPUBLISHER));

        GoogleCredential credential = new GoogleCredential.Builder().setTransport(readJsonFile.getTransport())
                .setJsonFactory(readJsonFile.getJsonFactory())
                .setServiceAccountId(readJsonFile.getServiceAccountId())
                .setServiceAccountScopes(readJsonFile.getServiceAccountScopes())
                //.setServiceAccountUser("hospital-billing-manager@api-7965197382587815639-857758.iam.gserviceaccount.com")
                .setServiceAccountPrivateKey(readJsonFile.getServiceAccountPrivateKey()).build();

        return credential;
    }

    public static void checkOrder() throws GeneralSecurityException, IOException {

        // 参数详细说明:
        String signtureData = "安卓上报的订单数据";
        String signture = "安卓上报的签名";
        String publicKey = "订单数据验签公钥";

        JSONObject parseObject = JSON.parseObject(signtureData);
        String productId = parseObject.getString("productId");//在谷歌后台定义的商品id
        String packageName = parseObject.getString("packageName");//安卓apk包名
        String purchaseToken = parseObject.getString("purchaseToken");//安卓上报的token
        int purchaseState = parseObject.getIntValue("purchaseState");//订单状态

        if (purchaseState != 0) {
            //TODO 订单未完成付款
            return;
        }

        Credential credential = authorizeServer();
        HttpTransport transport = GoogleNetHttpTransport.newTrustedTransport();

        AndroidPublisher publisher = new AndroidPublisher.Builder(transport, JacksonFactory.getDefaultInstance(),
                credential).setApplicationName("uu_oversea_pay").build();

        AndroidPublisher.Purchases.Products products = publisher.purchases().products();
        AndroidPublisher.Purchases.Subscriptions subscribes = publisher.purchases().subscriptions();

        boolean doCheck = RSA.doCheck(signtureData, signture, publicKey, "RSA1", true);
        // https://developers.google.com/android-publisher/api-ref/purchases/products/get
        AndroidPublisher.Purchases.Products.Get product = products.get(packageName, productId, purchaseToken);
        AndroidPublisher.Purchases.Subscriptions.Get subscribe = subscribes.get(packageName, productId,
                purchaseToken);

        // 获取订单信息
        // https://developers.google.com/android-publisher/api-ref/purchases/products
        // 通过consumptionState, purchaseState可以判断订单的状态
        String purchaseOrderId = "";
        int payType = 0;
        if (0 == payType) {
            ProductPurchase purchase = product.execute();
            purchaseOrderId = purchase.getOrderId();
            purchaseState = purchase.getPurchaseState();
            if (purchaseState != 0) {
                //TODO 订单未付款
                return;
            }
        } else {
            SubscriptionPurchase purchase = subscribe.execute();
            Long expiryTimeMillis = purchase.getExpiryTimeMillis();
            long now = System.currentTimeMillis() / 1000;
            if (now > expiryTimeMillis) {
                //TODO 订单已过订阅期限
                return;
            }
            purchaseOrderId = purchase.getOrderId();
        }
        //TODO 更改订单状态
    }

    public void googleRefundOrder() throws GeneralSecurityException, IOException {

        GoogleCredential credential = authorizeServer();
        String packageName = "安卓apk的包名";

        HttpTransport transport = GoogleNetHttpTransport.newTrustedTransport();
        JacksonFactory defaultInstance = JacksonFactory.getDefaultInstance();

        AndroidPublisher service = new AndroidPublisher.Builder(transport, defaultInstance, credential)
                .setApplicationName("uu_oversea_pay").build();

        // 获取google list对象
        AndroidPublisher.Purchases.Voidedpurchases.List voidPurchaseList = service.purchases().voidedpurchases()
                .list(packageName);
        // 设置查询参数
        voidPurchaseList.setStartTime(getDaysBeforeUnixTimeStampMinute(-24 * 60));//一天
        // 执行查询
        VoidedPurchasesListResponse response = voidPurchaseList.execute();
        List<VoidedPurchase> voidedPurchases = response.getVoidedPurchases();
        if (voidedPurchases == null) {
            //TODO 没有退款
            return;
        }
        // 获取分页tokenPagination
        TokenPagination tokenPagination = response.getTokenPagination();
        while (tokenPagination != null) {
            // 设置查询token 重新执行查询 查询下一页
            voidPurchaseList.setToken(tokenPagination.getNextPageToken());
            VoidedPurchasesListResponse newResponse = voidPurchaseList.execute();
            voidedPurchases.addAll(newResponse.getVoidedPurchases());
            tokenPagination = newResponse.getTokenPagination();
        }
        for (VoidedPurchase voidedPurchase : voidedPurchases) {
            String orderId = voidedPurchase.getOrderId();
            //TODO 处理相关退款账号和设备
        }
    }

    public void googleSubscribeOrder(byte[] body) throws IOException, GeneralSecurityException {
        JSONObject paramJson = null;

        String paramStr = new String(body, "utf-8");

        if (StringUtils.isNotBlank(paramStr)) {
            paramJson = JSON.parseObject(URLDecoder.decode(paramStr, "utf-8"));

            JSONObject msgJson = paramJson.getJSONObject("message");
            String data = msgJson.getString("data");
            String developerNotificationStr = new String(Base64.getDecoder().decode(data), "UTF-8");
            JSONObject developerNotificationJson = JSON.parseObject(developerNotificationStr);
            String packageName = developerNotificationJson.getString("packageName");
            JSONObject subscriptionNotificationJson = developerNotificationJson
                    .getJSONObject("subscriptionNotification");
            String purchaseToken = subscriptionNotificationJson.getString("purchaseToken");
            String subscriptionId = subscriptionNotificationJson.getString("subscriptionId");
            /**
             * notificationType int 通知的类型。它可以具有以下值： (1)
             * SUBSCRIPTION_RECOVERED - 从帐号保留状态恢复了订阅。 (2)
             * SUBSCRIPTION_RENEWED - 续订了处于活动状态的订阅。 (3)
             * SUBSCRIPTION_CANCELED - 自愿或非自愿地取消了订阅。如果是自愿取消，在用户取消时发送。 (4)
             * SUBSCRIP￼￼TION_PURCHASED - 购买了新的订阅。 (5) SUBSCRIPTION_ON_HOLD
             * - 订阅已进入帐号保留状态（如已启用）。 (6) SUBSCRIPTION_IN_GRACE_PERIOD -
             * 订阅已进入宽限期（如已启用）。 (7) SUBSCRIPTION_RESTARTED -
             * 用户已通过“Play”>“帐号”>“订阅”重新激活其订阅（需要选择使用订阅恢复功能）。 (8)
             * SUBSCRIPTION_PRICE_CHANGE_CONFIRMED - 用户已成功确认订阅价格变动。 (9)
             * SUBSCRIPTION_DEFERRED - 订阅的续订时间点已延期。 (10) SUBSCRIPTION_PAUSED
             * - 订阅已暂停。 (11) SUBSCRIPTION_PAUSE_SCHEDULE_CHANGED -
             * 订阅暂停计划已更改。 (12) SUBSCRIPTION_REVOKED - 用户在有效时间结束前已撤消订阅。 (13)
             * SUBSCRIPTION_EXPIRED - 订阅已过期。
             */
            int notificationType = subscriptionNotificationJson.getIntValue("notificationType");

            if (2 == notificationType) {

                GoogleCredential credential = authorizeServer();

                HttpTransport transport = GoogleNetHttpTransport.newTrustedTransport();

                AndroidPublisher publisher = new AndroidPublisher.Builder(transport,
                        JacksonFactory.getDefaultInstance(), credential).setApplicationName("uu_oversea_pay")
                        .build();

                AndroidPublisher.Purchases.Subscriptions subscribes = publisher.purchases().subscriptions();

                AndroidPublisher.Purchases.Subscriptions.Get subscribe = subscribes.get(packageName, subscriptionId,
                        purchaseToken);

                SubscriptionPurchase purchase = subscribe.execute();
                Long expiryTimeMillis = purchase.getExpiryTimeMillis();
                long now = System.currentTimeMillis() / 1000;
                if (now > expiryTimeMillis) {
                    //已过订阅期限
                    return;
                }
                String purchaseOrderId = purchase.getOrderId();
                //TODO 续订
            }
        }
    }

    private Long getDaysBeforeUnixTimeStampMinute(int time) {//分钟
        Calendar beforeTime = Calendar.getInstance();
        beforeTime.add(Calendar.MINUTE, -time);// 5分钟之前的时间
        Date beforeD = beforeTime.getTime();
        return beforeD.getTime();
    }

    public static void main(String[] args) {
        try {
            Credential credential = authorizeServer();

            System.out.println(credential);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void refreshToken(String code) {
        Map<String, Object> params = new HashMap<>();
        try {
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            InputStreamReader isr = new InputStreamReader(
                    GooglePayServiceImpl.class.getClassLoader().getResourceAsStream("./client_secrets.json"));
            GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(jsonFactory, isr);
            String clientId = clientSecrets.getDetails().getClientId();
            String clientSecret = clientSecrets.getDetails().getClientSecret();

            params.put("code", code);
            params.put("client_id", clientId);
            params.put("client_secret", clientSecret);
            params.put("redirect_uri", "http://localhost:8092/jingxc/google/google-callback");
            params.put("grant_type", "authorization_code");

            Map<String, String> res = client.postByFormDataToMap("https://accounts.google.com/o/oauth2/token",
                    new HashMap<>(), params);

            System.out.println(JSON.toJSONString(res));
        } catch (Exception e) {

            e.printStackTrace();
        }
    }
}
