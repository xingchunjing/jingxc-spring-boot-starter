package top.jingxc.server;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication
@MapperScan(basePackages = "top.jingxc.server.mapper")
public class PaypalApplication {
    public static void main(String[] args) {
        SpringApplication.run(PaypalApplication.class, args);
    }
}
