package top.jingxc.server.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import top.jingxc.server.service.GooglePayService;

@RestController
@RequestMapping
public class GooglePayController extends BaseController {

    @Autowired
    private GooglePayService googlePayServiceImpl;

    @GetMapping({"/google-callback"})
    public void test(@RequestParam("code") String code) {
        System.out.println(code);
        googlePayServiceImpl.refreshToken(code);
    }

    /**
     * 获取refresh_token
     */
    @GetMapping({"/refreshToken"})
    public void refreshToken(@RequestParam("code") String code) {
        googlePayServiceImpl.refreshToken(code);
    }
}
