package top.jingxc.server.config;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class RabbitQueueConfig {

    public final static String TEST_WORK_QUEUE = "TEST_WORK_QUEUE";
    public final static String TEST_FANOUT_SEND_MESSAGES_QUEUE = "TEST_FANOUT_SEND_MESSAGES_QUEUE";
    public final static String TEST_FANOUT_SEND_MESSAGES_EXCHANGE = "TEST_FANOUT_SEND_MESSAGES_EXCHANGE";

    public final static String TEST_ROUTING_QUEUE_A = "TEST_ROUTING_QUEUE_A";
    public final static String TEST_ROUTING_QUEUE_B = "TEST_ROUTING_QUEUE_B";
    public final static String TEST_ROUTING_EXCHANGE = "TEST_ROUTING_EXCHANGE";

    public final static String DIRECT = "direct";
    public final static String FANOUNT = "fanout";

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

    @Bean
    public Queue routingQueueA() {
        return new Queue(TEST_ROUTING_QUEUE_A);
    }

    @Bean
    public Queue routingQueueB() {
        return new Queue(TEST_ROUTING_QUEUE_B);
    }

    /**
     * 声明fanout交换机
     */
    @Bean("fanoutExchange")
    public FanoutExchange fanoutExchange() {
        return new FanoutExchange(TEST_FANOUT_SEND_MESSAGES_EXCHANGE);
    }

    @Bean("routingExchange")
    public DirectExchange routingExchange() {
        // 创建direct类型交换机，表示与此交换机会将消息发送给 routing_key 完全相同的队列
        return new DirectExchange(TEST_ROUTING_EXCHANGE);
    }

    @Bean
    Binding bindingFanoutExchangeFanoutQueue() {
        return BindingBuilder.bind(fanoutQueue()).to(fanoutExchange());
    }

    @Bean
    public Binding bindExchangeQueueA(Queue routingQueueA, @Qualifier("routingExchange") DirectExchange routingExchange) {
        // 绑定direct交换机，并设置 routing_key 为 routing_second_queue_routing_key
        return BindingBuilder.bind(routingQueueA).to(routingExchange).with("217");
    }

    @Bean
    public Binding bindExchangeQueueB(Queue routingQueueB, @Qualifier("routingExchange") DirectExchange routingExchange) {
        // 队列二绑定direct交换机，并设置 routing_key 为 routing_second_queue_routing_key
        return BindingBuilder.bind(routingQueueB).to(routingExchange).with("218");
    }
}
