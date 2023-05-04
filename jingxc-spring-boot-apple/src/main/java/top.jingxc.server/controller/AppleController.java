package top.jingxc.server.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import top.jingxc.server.config.ReturnResult;
import top.jingxc.server.service.AppleService;

@RestController
@RequestMapping("")
public class AppleController extends BaseController {

    @Autowired
    private AppleService appleServiceImpl;

    @PostMapping("/check/verifyReceipt")
    private ReturnResult verifyReceipt(@RequestParam("receipt") String receipt) {
        return appleServiceImpl.verifyReceipt(receipt);
    }

    @RequestMapping("/refund/v1")
    private ReturnResult refundV1(String data) {
        return appleServiceImpl.refundV1(data);
    }

    @RequestMapping("/refund/v2")
    private ReturnResult refundV2(String data) {
        return appleServiceImpl.refundV2(data);
    }
}
