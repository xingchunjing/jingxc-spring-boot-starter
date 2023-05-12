package top.jingxc.server.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.rabbitmq.client.Channel;
import lombok.extern.log4j.Log4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.stereotype.Service;
import top.jingxc.server.service.RabbitMQService;

import java.io.IOException;

@Log4j
@Service
public class RabbitMQServiceImpl implements RabbitMQService {

    @RabbitListener(queuesToDeclare = @Queue(value = "TEST_WORK_QUEUE"))
    @RabbitHandler
    public void work(String msg, Channel channel, Message message) throws IOException {
        try {
            log.info("收到消息：" + msg);
            JSONObject jsonObject = JSON.parseObject(msg);

            /**
             * basicAck：表示成功确认，使用此回执方法后，消息会被rabbitmq broker 删除。
             * void basicAck(long deliveryTag, boolean multiple)
             * deliveryTag：表示消息投递序号，每次消费消息或者消息重新投递后，deliveryTag都会增加。手动消息确认模式下，我们可以对指定deliveryTag的消息进行ack、nack、reject等操作。
             * multiple：是否批量确认，值为 true 则会一次性 ack所有小于当前消息 deliveryTag 的消息。
             * */
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);

            log.info("deliveryTag：" + message.getMessageProperties().getDeliveryTag());
            log.info("redelivered: " + message.getMessageProperties().getRedelivered());
            //TODO 具体业务
        } catch (Exception e) {
            if (message.getMessageProperties().getRedelivered()) {
                log.info("消息已重复处理失败,拒绝再次接收！");
                /**
                 * basicReject：拒绝消息，与basicNack区别在于不能进行批量操作，其他用法很相似。
                 * deliveryTag：表示消息投递序号。
                 * requeue：值为 true 消息将重新入队列。
                 */
                channel.basicReject(message.getMessageProperties().getDeliveryTag(), false);
                // 拒绝消息，requeue=false 表示不再重新入队，如果配置了死信队列则进入死信队列
            } else {
                log.info("消息即将再次返回队列处理！");
                /**
                 * deliveryTag：表示消息投递序号。
                 * multiple：是否批量确认。
                 * requeue：值为 true 消息将重新入队列。
                 */
                channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
                // requeue为是否重新回到队列，true重新入队
            }
        }
    }

    @RabbitListener(bindings = {
            @QueueBinding(
                    value = @Queue(value = "TEST_FANOUT_SEND_MESSAGES_QUEUE"),
                    exchange = @Exchange(value = "TEST_FANOUT_SEND_MESSAGES_EXCHANGE", type = "fanout")//绑定交换机
            )
    })
    public void fanout(String msg, Channel channel, Message message) throws IOException {
        try {
            log.info("收到消息：" + msg);
            JSONObject jsonObject = JSON.parseObject(msg);

            /**
             * basicAck：表示成功确认，使用此回执方法后，消息会被rabbitmq broker 删除。
             * void basicAck(long deliveryTag, boolean multiple)
             * deliveryTag：表示消息投递序号，每次消费消息或者消息重新投递后，deliveryTag都会增加。手动消息确认模式下，我们可以对指定deliveryTag的消息进行ack、nack、reject等操作。
             * multiple：是否批量确认，值为 true 则会一次性 ack所有小于当前消息 deliveryTag 的消息。
             * */
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            log.info("deliveryTag：" + message.getMessageProperties().getDeliveryTag());
            log.info("redelivered: " + message.getMessageProperties().getRedelivered());
            //TODO 具体业务
        } catch (Exception e) {
            if (message.getMessageProperties().getRedelivered()) {
                System.out.println("消息已重复处理失败,拒绝再次接收！");
                // 拒绝消息，requeue=false 表示不再重新入队，如果配置了死信队列则进入死信队列
                /**
                 * basicReject：拒绝消息，与basicNack区别在于不能进行批量操作，其他用法很相似。
                 * deliveryTag：表示消息投递序号。
                 * requeue：值为 true 消息将重新入队列。
                 */
                channel.basicReject(message.getMessageProperties().getDeliveryTag(), false);
            } else {
                System.out.println("消息即将再次返回队列处理！");
                // requeue为是否重新回到队列，true重新入队
                /**
                 * deliveryTag：表示消息投递序号。
                 * multiple：是否批量确认。
                 * requeue：值为 true 消息将重新入队列。
                 */
                channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
            }
        }
    }

    /**
     * rouing 路由模式 同一路由下 接收相同的数据。
     */

    @RabbitListener(bindings = {
            @QueueBinding(
                    value = @Queue(value = "TEST_ROUTING_QUEUE_A"),
                    exchange = @Exchange(value = "TEST_ROUTING_EXCHANGE", type = "direct"),
                    key = {"217"}
            )
    })
    public void routing0(String msg, Channel channel, Message message) throws IOException {
        try {
            log.info("收到消息：" + msg);
            JSONObject jsonObject = JSON.parseObject(msg);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            log.info("进入routing0" + msg);
            //TODO 具体业务
        } catch (Exception e) {
            if (message.getMessageProperties().getRedelivered()) {
                System.out.println("消息已重复处理失败,拒绝再次接收！");
                // 拒绝消息，requeue=false 表示不再重新入队，如果配置了死信队列则进入死信队列
                channel.basicReject(message.getMessageProperties().getDeliveryTag(), false);
            } else {
                System.out.println("消息即将再次返回队列处理！");
                // requeue为是否重新回到队列，true重新入队
                channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
            }
        }
    }

    /**
     * rouing 路由模式 同一路由下 接收相同的数据。
     */
    @RabbitListener(bindings = {
            @QueueBinding(
                    value = @Queue(value = "TEST_ROUTING_QUEUE_B"),
                    exchange = @Exchange(value = "TEST_ROUTING_EXCHANGE", type = "direct"),
                    key = {"217"}
            )
    })
    public void routing1(String msg, Channel channel, Message message) throws IOException {
        try {
            log.info("收到消息：" + msg);
            JSONObject jsonObject = JSON.parseObject(msg);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            log.info("进入routing1" + msg);
            //TODO 具体业务
        } catch (Exception e) {
            if (message.getMessageProperties().getRedelivered()) {
                System.out.println("消息已重复处理失败,拒绝再次接收！");
                // 拒绝消息，requeue=false 表示不再重新入队，如果配置了死信队列则进入死信队列
                channel.basicReject(message.getMessageProperties().getDeliveryTag(), false);
            } else {
                System.out.println("消息即将再次返回队列处理！");
                // requeue为是否重新回到队列，true重新入队
                channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
            }
        }
    }

    @RabbitListener(bindings = {
            @QueueBinding(
                    value = @Queue(value = "TEST_ROUTING_QUEUE_B"),
                    exchange = @Exchange(value = "TEST_ROUTING_EXCHANGE", type = "direct"),
                    key = {"217", "218"}
            )
    })
    public void routing2(String msg, Channel channel, Message message) throws IOException {
        try {
            log.info("收到消息：" + msg);
            JSONObject jsonObject = JSON.parseObject(msg);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            log.info("进入routing2" + msg);
            //TODO 具体业务
        } catch (Exception e) {
            if (message.getMessageProperties().getRedelivered()) {
                System.out.println("消息已重复处理失败,拒绝再次接收！");
                // 拒绝消息，requeue=false 表示不再重新入队，如果配置了死信队列则进入死信队列
                channel.basicReject(message.getMessageProperties().getDeliveryTag(), false);
            } else {
                System.out.println("消息即将再次返回队列处理！");
                // requeue为是否重新回到队列，true重新入队
                channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
            }
        }
    }
}
