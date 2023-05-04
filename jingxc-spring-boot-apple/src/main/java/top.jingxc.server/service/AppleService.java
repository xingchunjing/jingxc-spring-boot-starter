package top.jingxc.server.service;

import top.jingxc.server.config.ReturnResult;

public interface AppleService {

    /**
     * 校验数据
     *
     * @param receipt
     * @return
     */
    ReturnResult verifyReceipt(String receipt);

    /**
     * v1退款
     *
     * @param data
     * @return
     */
    ReturnResult refundV1(String data);

    /**
     * v2退款
     *
     * @param data
     * @return
     */
    ReturnResult refundV2(String data);
}
