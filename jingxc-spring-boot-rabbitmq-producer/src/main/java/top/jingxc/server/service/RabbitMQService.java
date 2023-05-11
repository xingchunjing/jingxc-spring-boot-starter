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
}
