package com.huwei.gmall0416.cart;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.huwei.gmall0416")
@MapperScan(basePackages = "com.huwei.gmall0416.cart.mapper")
public class GmallCartServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(GmallCartServiceApplication.class, args);
        System.out.println("启动成功");
    }

}
