package top.jingxc.server.service;

import top.jingxc.server.config.ReturnResult;
import top.jingxc.server.param.CreateOrderParams;
import top.jingxc.server.param.CreateTokenParams;
import top.jingxc.server.param.RefundOrderParams;

public interface XsollaService {

    /**
     * 创建用户认证令牌
     *
     * @param params
     * @return
     */
    ReturnResult createToken(CreateTokenParams params);

    /**
     * 使用指定商品创建订单
     *
     * @param params
     * @return
     */
    ReturnResult createOrder(CreateOrderParams params);

    /**
     * 退款
     *
     * @param params
     * @return
     */
    ReturnResult refundOrder(RefundOrderParams params);
}
