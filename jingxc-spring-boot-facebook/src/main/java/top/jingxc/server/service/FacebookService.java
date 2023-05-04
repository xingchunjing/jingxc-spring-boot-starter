package top.jingxc.server.service;

import top.jingxc.server.config.ReturnResult;

import javax.servlet.http.HttpServletRequest;

public interface FacebookService {

    /**
     * 登陆回调
     *
     * @param req
     * @return
     */
    ReturnResult loginCallback(HttpServletRequest req);

    /**
     * 校验token
     *
     * @param accessToken
     * @return
     */
    ReturnResult checkToken(String accessToken);

    /**
     * 获取用户信息
     *
     * @param accessToken
     * @return
     */
    ReturnResult checkUser(String accessToken, String userId);
}
