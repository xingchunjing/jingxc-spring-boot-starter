package top.jingxc.server.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.jingxc.server.service.RabbitMQService;

@RestController
@RequestMapping("/producer")
public class RabbitMQController extends BaseController {

    @Autowired
    private RabbitMQService rabbitMQServiceImpl;
}
