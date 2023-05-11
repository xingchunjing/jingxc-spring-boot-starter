package top.jingxc.server.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class RabbitQueueConfig {

    public final static String TEST_WORK_QUEUE = "TEST_WORK_QUEUE";
    public final static String TEST_FANOUT_SEND_MESSAGES_QUEUE = "TEST_FANOUT_SEND_MESSAGES_QUEUE";
    public final static String TEST_FANOUT_SEND_MESSAGES_EXCHANGE = "TEST_FANOUT_SEND_MESSAGES_EXCHANGE";

    /**
     * work queue 模型
     */
    @Bean
    Queue workQueue() {
        return new Queue(TEST_WORK_QUEUE);
    }

    /**
     * 声明fanout 队列
     */
    @Bean
    public Queue fanoutQueue() {
        return new Queue(TEST_FANOUT_SEND_MESSAGES_QUEUE);
    }

    /**
     * 声明fanout交换机
     */
    @Bean
    public FanoutExchange fanoutExchange() {
        return new FanoutExchange(TEST_FANOUT_SEND_MESSAGES_EXCHANGE);
    }

    @Bean
    Binding bindingFanoutExchangeOrderDicQueue() {
        return BindingBuilder.bind(fanoutQueue()).to(fanoutExchange());
    }
}
