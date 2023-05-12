package top.jingxc.server.service.impl;

import com.alibaba.fastjson.JSON;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.jingxc.server.aop.OperationLogger;
import top.jingxc.server.config.ConstantCommon;
import top.jingxc.server.config.RabbitQueueConfig;
import top.jingxc.server.config.ReturnResult;
import top.jingxc.server.config.ReturnResultSuccess;
import top.jingxc.server.service.RabbitMQService;

import java.util.HashMap;
import java.util.Map;

@Service
public class RabbitMQServiceImpl implements RabbitMQService {

    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private ConfirmCallbackServiceImpl confirmCallbackServiceImpl;
    @Autowired
    private ReturnCallbackServiceImpl returnCallbackServiceImpl;
    @Autowired
    private Queue workQueue;
    @Autowired
    private RabbitQueueConfig rabbitQueueConfig;

    @Override
    @OperationLogger
    public ReturnResult work() {

        for (int i = 0; i < 5; i++) {

            String gameId = "217";
            String channelId = "2000100000";

            Map<String, Object> msg = new HashMap<>();
            msg.put("gameId", gameId);
            msg.put("channelId", channelId);
            msg.put("orderId", i);

            CorrelationData correlationData = new CorrelationData(gameId + "-" + channelId + "-" + System.currentTimeMillis());
            rabbitTemplate.setConfirmCallback(confirmCallbackServiceImpl);
            rabbitTemplate.setReturnCallback(returnCallbackServiceImpl);

            rabbitTemplate.convertAndSend(workQueue.getName(), JSON.toJSONString(msg), message -> {
                //MessageProperties 封装消息的一些属性，属性比较多，我这里只设置以下消息的持久化，PERSISTENT-持久化  NON_PERSISTENT-非持久化
                message.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                return message;
            }, correlationData);
        }
        return ReturnResultSuccess.builder().code(ConstantCommon.RETURN_CODE_200).msg("success").data("")
                .count(ConstantCommon.RETURN_COUNT_1).build();
    }

    @Override
    @OperationLogger
    public ReturnResult fanout() {
        for (int i = 0; i < 5; i++) {
            String gameId = "217";
            String channelId = "2000100000";

            Map<String, Object> msg = new HashMap<>();
            msg.put("gameId", gameId);
            msg.put("channelId", channelId);
            msg.put("orderId", i);

            CorrelationData correlationData = new CorrelationData(gameId + "-" + channelId + "-" + System.currentTimeMillis());

            rabbitTemplate.setConfirmCallback(confirmCallbackServiceImpl);
            rabbitTemplate.setReturnCallback(returnCallbackServiceImpl);
            rabbitTemplate.convertAndSend(rabbitQueueConfig.TEST_FANOUT_SEND_MESSAGES_EXCHANGE, rabbitQueueConfig.TEST_FANOUT_SEND_MESSAGES_QUEUE,
                    JSON.toJSONString(msg), message -> {
                        message.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                        return message;
                    }, correlationData);
        }
        return ReturnResultSuccess.builder().code(ConstantCommon.RETURN_CODE_200).msg("success").data("")
                .count(ConstantCommon.RETURN_COUNT_1).build();
    }

    @Override
    @OperationLogger
    public ReturnResult routing(String gameId) {

        String channelId = "2000100000";

        Map<String, Object> msg = new HashMap<>();
        msg.put("gameId", gameId);
        msg.put("channelId", channelId);

        CorrelationData correlationData = new CorrelationData(gameId + "-" + channelId + "-" + System.currentTimeMillis());

        rabbitTemplate.setConfirmCallback(confirmCallbackServiceImpl);
        rabbitTemplate.setReturnCallback(returnCallbackServiceImpl);
        String routingKey = gameId;
        rabbitTemplate.convertAndSend(rabbitQueueConfig.TEST_ROUTING_EXCHANGE, routingKey, JSON.toJSONString(msg), message -> {
            message.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
            return message;
        }, correlationData);

        return ReturnResultSuccess.builder().code(ConstantCommon.RETURN_CODE_200).msg("success").data("")
                .count(ConstantCommon.RETURN_COUNT_1).build();
    }

    @Override
    @OperationLogger
    public ReturnResult topic(String gameId, String channelId) {

        Map<String, Object> msg = new HashMap<>();
        msg.put("gameId", gameId);
        msg.put("channelId", channelId);

        CorrelationData correlationData = new CorrelationData(gameId + "-" + channelId + "-" + System.currentTimeMillis());

        rabbitTemplate.setConfirmCallback(confirmCallbackServiceImpl);
        rabbitTemplate.setReturnCallback(returnCallbackServiceImpl);
        String routingKey = gameId + "." + channelId;
        rabbitTemplate.convertAndSend(rabbitQueueConfig.TEST_TOPIC_EXCHANGE, routingKey, JSON.toJSONString(msg), message -> {
            message.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
            return message;
        }, correlationData);

        return ReturnResultSuccess.builder().code(ConstantCommon.RETURN_CODE_200).msg("success").data("")
                .count(ConstantCommon.RETURN_COUNT_1).build();
    }

}
