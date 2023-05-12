package top.jingxc.server.service;

import top.jingxc.server.config.ReturnResult;

public interface RabbitMQService {

    /**
     * work模式
     *
     * @return
     */
    ReturnResult work();

    /**
     * Publish/Subscribe（发布订阅模式）
     *
     * @return
     */
    ReturnResult fanout();

    /**
     * 路由模式
     *
     * @return
     */
    ReturnResult routing(String gameId);

    /**
     * 主题模式
     *
     * @param gameId
     * @param channelId
     * @return
     */
    ReturnResult topic(String gameId, String channelId);
}
