package top.jingxc.server.service.impl;

import com.alibaba.fastjson.JSON;
import lombok.extern.log4j.Log4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Log4j
@Component
public class ReturnCallbackServiceImpl implements RabbitTemplate.ReturnCallback {

    @Override
    public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey) {

        Map<String, Object> msg = new HashMap<>();
        msg.put("message", message);
        msg.put("replyCode", replyCode);
        msg.put("replyText", replyText);
        msg.put("exchange", exchange);
        msg.put("routingKey", routingKey);

        log.info(JSON.toJSONString(msg));

        msg = null;

    }
}
