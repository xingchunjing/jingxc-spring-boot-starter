package top.jingxc.server.service;

public interface GooglePayService {
    /**
     * 获取refresh_token
     */
    void refreshToken(String code);
}
