package top.jingxc.server.service.impl;

import com.alibaba.fastjson.JSON;
import lombok.extern.log4j.Log4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Log4j
@Component
public class ConfirmCallbackServiceImpl implements RabbitTemplate.ConfirmCallback {

    @Override
    public void confirm(CorrelationData correlationData, boolean ack, String cause) {

        if (!ack) {
            log.error("消息发送异常!");
            // 发送异常处理
        } else {
            Map<String, Object> msg = new HashMap<>();
            msg.put("correlationData", correlationData);
            msg.put("ack", ack);
            msg.put("cause", cause);
            log.info(JSON.toJSONString(msg));
            msg = null;
        }
    }
}
