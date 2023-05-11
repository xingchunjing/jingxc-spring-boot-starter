package top.jingxc.server.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.jingxc.server.config.ReturnResult;
import top.jingxc.server.service.RabbitMQService;

@RestController
@RequestMapping("/producer")
public class RabbitMQController extends BaseController {

    @Autowired
    private RabbitMQService rabbitMQServiceImpl;

    /**
     * work 模式
     *
     * @return
     */
    @GetMapping("/work")
    public ReturnResult work() {
        return rabbitMQServiceImpl.work();
    }

    /**
     * Publish/Subscribe（发布订阅模式）
     *
     * @return
     */
    @GetMapping("/fanout")
    public ReturnResult fanout() {
        return rabbitMQServiceImpl.fanout();
    }
}
