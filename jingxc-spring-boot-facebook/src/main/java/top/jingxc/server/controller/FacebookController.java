package top.jingxc.server.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import top.jingxc.server.config.ReturnResult;
import top.jingxc.server.service.FacebookService;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping
public class FacebookController extends BaseController {

    @Autowired
    private FacebookService facebookServiceImpl;

    @GetMapping("/login/callback")
    private ReturnResult loginCallback(HttpServletRequest req) {
        return facebookServiceImpl.loginCallback(req);
    }

    @GetMapping("/check/token")
    private ReturnResult checkToken(@RequestParam("accessToken") String accessToken) {
        return facebookServiceImpl.checkToken(accessToken);
    }

    @GetMapping("/check/user")
    private ReturnResult checkUser(@RequestParam("accessToken") String accessToken,
                                   @RequestParam("userId") String userId) {
        return facebookServiceImpl.checkUser(accessToken, userId);
    }
}
