package top.jingxc.server.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.jingxc.server.config.ReturnResult;
import top.jingxc.server.exception.ReturnCode200Exception;
import top.jingxc.server.param.CreateOrderParams;
import top.jingxc.server.param.CreateTokenParams;
import top.jingxc.server.param.RefundOrderParams;
import top.jingxc.server.service.XsollaService;

import javax.validation.Valid;

@RestController
@RequestMapping
public class XsollaController extends BaseController {

    @Autowired
    private XsollaService xsollaServiceImpl;

    /**
     * 创建用户认证令牌
     *
     * @param params
     * @param bindingResult
     * @return
     */
    @PostMapping({"/create/token"})
    public ReturnResult createToken(@Valid CreateTokenParams params, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            throw new ReturnCode200Exception(bindingResult.getFieldError().getDefaultMessage());
        }

        return xsollaServiceImpl.createToken(params);

    }

    /**
     * 使用指定商品创建订单
     *
     * @param params
     * @param bindingResult
     * @return
     */
    @PostMapping({"/create/order"})
    public ReturnResult createOrder(@Valid CreateOrderParams params, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            throw new ReturnCode200Exception(bindingResult.getFieldError().getDefaultMessage());
        }

        return xsollaServiceImpl.createOrder(params);

    }

    /**
     * 退款
     *
     * @param params
     * @param bindingResult
     * @return
     */
    @PostMapping({"/refund/order"})
    public ReturnResult refundOrder(@Valid RefundOrderParams params, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            throw new ReturnCode200Exception(bindingResult.getFieldError().getDefaultMessage());
        }

        return xsollaServiceImpl.refundOrder(params);

    }
}
