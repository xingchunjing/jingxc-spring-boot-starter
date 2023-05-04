package top.jingxc.server.service.impl;

import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.jingxc.server.aop.OperationLogger;
import top.jingxc.server.client.service.FeignClientService;
import top.jingxc.server.config.ConstantCommon;
import top.jingxc.server.config.ReturnResult;
import top.jingxc.server.config.ReturnResultSuccess;
import top.jingxc.server.service.FacebookService;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Service
public class FacebookServiceImpl implements FacebookService {

    @Autowired
    private FeignClientService client;

    @Override
    public ReturnResult loginCallback(HttpServletRequest req) {

        Map<String, String[]> parameterMap = req.getParameterMap();
        System.out.println(JSON.toJSONString(parameterMap));

        return ReturnResultSuccess.builder().code(ConstantCommon.RETURN_CODE_200).msg("success")
                .data("").build();
    }

    @Override
    @OperationLogger
    public ReturnResult checkToken(String accessToken) {
        String url = "https://graph.facebook.com/debug_token?input_token=" +
                accessToken + "&access_token=" + accessToken;

        Map<String, String> checkToken = client.getToMap(url, new HashMap<>(), new HashMap<>());

        System.out.println(JSON.toJSONString(checkToken));
        return ReturnResultSuccess.builder().code(ConstantCommon.RETURN_CODE_200).msg("success")
                .data(checkToken).build();
    }

    @Override
    @OperationLogger
    public ReturnResult checkUser(String accessToken, String userId) {
        String url = "https://graph.facebook.com/" + userId + "?fields=id,name"
                + "&access_token=" + accessToken;

        Map<String, String> checkUser = client.getToMap(url, new HashMap<>(), new HashMap<>());

        System.out.println(JSON.toJSONString(checkUser));
        return ReturnResultSuccess.builder().code(ConstantCommon.RETURN_CODE_200).msg("success")
                .data(checkUser).build();
    }
}
