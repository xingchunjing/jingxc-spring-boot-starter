package top.jingxc.server.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.jingxc.server.config.ReturnResult;
import top.jingxc.server.service.ShardingSphereService;

@RestController
@RequestMapping("")
public class ShardingSphereController {

    @Autowired
    private ShardingSphereService shardingSphereServiceImpl;

    @GetMapping()
    public ReturnResult test() {
        return shardingSphereServiceImpl.test();
    }
}
