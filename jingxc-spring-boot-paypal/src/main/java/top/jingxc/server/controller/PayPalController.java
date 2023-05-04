package top.jingxc.server.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.jingxc.server.config.ReturnResult;
import top.jingxc.server.exception.ReturnCode200Exception;
import top.jingxc.server.param.*;
import top.jingxc.server.service.PaypalService;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@RestController
@RequestMapping("")
public class PayPalController extends BaseController {

    @Autowired
    private PaypalService paypalServiceImpl;

    /**
     * 创建订单
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

        return paypalServiceImpl.createOrder(params);

    }

    /**
     * 捕获订单执行扣款
     *
     * @param params
     * @param bindingResult
     * @return
     */
    @PostMapping({"/capture/order"})
    public ReturnResult captureOrder(@Valid CaptureOrderParams params, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            throw new ReturnCode200Exception(bindingResult.getFieldError().getDefaultMessage());
        }

        return paypalServiceImpl.captureOrder(params);

    }

    /**
     * 查看订单状态
     *
     * @param params
     * @param bindingResult
     * @return
     */
    @PostMapping({"/check/order"})
    public ReturnResult checkOrder(@Valid CheckOrderParams params, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            throw new ReturnCode200Exception(bindingResult.getFieldError().getDefaultMessage());
        }

        return paypalServiceImpl.checkOrder(params);

    }

    /**
     * 查看订单捕获状态
     *
     * @param params
     * @param bindingResult
     * @return
     */
    @PostMapping({"/check/capture"})
    public ReturnResult checkCapture(@Valid CheckCaptureParams params, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            throw new ReturnCode200Exception(bindingResult.getFieldError().getDefaultMessage());
        }

        return paypalServiceImpl.checkCapture(params);

    }

    /**
     * 申请退款
     *
     * @param params
     * @param bindingResult
     * @return
     */
    @PostMapping({"/refund/capture"})
    public ReturnResult refundCapture(@Valid RefundCaptureParams params, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            throw new ReturnCode200Exception(bindingResult.getFieldError().getDefaultMessage());
        }

        return paypalServiceImpl.refundCapture(params);

    }

    @RequestMapping({"/webhooks"})
    public void webhooks(HttpServletRequest req, @RequestBody String body) {
        paypalServiceImpl.webhooks(req, body);
    }

}
