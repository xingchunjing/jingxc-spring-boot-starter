package top.jingxc.server.config;

public class ConstantCommon {

    //订单状态，创建订单
    public static final int ORDER_STATUS_0 = 0;
    //订单状态，捕获订单
    public static final int ORDER_STATUS_1 = 1;
    //订单状态，退款订单
    public static final int ORDER_STATUS_2 = 2;

    //苹果订单返回码
    public static final String RECEIPT_RETURN_STATUS_21007 = "21007";
    public static final String RECEIPT_RETURN_STATUS_0 = "0";

    //成功
    public static final int RETURN_CODE_200 = 200;
    //通用失败
    public static final int RETURN_CODE_999 = 999;
    //参数校验失败
    public static final int RETURN_CODE_902 = 902;
    //账号不存在
    public static final int RETURN_CODE_903 = 903;

    public static final Integer RETURN_COUNT_1 = 1;

    public static final String KEY_EXTRA_CLIENT = "JINGXC";
    //商品key前缀
    public static final String KEY_PREFIX_PRODUCT = "KEY_PREFIX_PRODUCT_";
    //配置key前缀
    public static final String KEY_PREFIX_CONSTANT = "KEY_PREFIX_CONSTANT_";
}
