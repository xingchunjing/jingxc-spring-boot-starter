package top.jingxc.server.service;

import top.jingxc.server.config.ReturnResult;
import top.jingxc.server.param.*;

import javax.servlet.http.HttpServletRequest;

public interface PaypalService {
    /**
     * 创建订单
     *
     * @param params
     * @return
     */
    ReturnResult createOrder(CreateOrderParams params);

    /**
     * 捕获订单执行扣款
     *
     * @param params
     * @return
     */
    ReturnResult captureOrder(CaptureOrderParams params);

    /**
     * 查看订单状态
     *
     * @param params
     * @return
     */
    ReturnResult checkOrder(CheckOrderParams params);

    /**
     * 查看订单捕获状态
     *
     * @param params
     * @return
     */
    ReturnResult checkCapture(CheckCaptureParams params);

    /**
     * 申请退款
     *
     * @param params
     * @return
     */
    ReturnResult refundCapture(RefundCaptureParams params);

    /**
     * 事件推送
     *
     * @param req
     * @param body
     */
    void webhooks(HttpServletRequest req, String body);
}
