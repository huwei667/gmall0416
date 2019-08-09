package com.huwei.gmall0416.usermanage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import tk.mybatis.spring.annotation.MapperScan;


@SpringBootApplication
@MapperScan(basePackages = "com.huwei.gmall0416.usermanage.mapper")
@ComponentScan(basePackages ="com.huwei.gmall0416")
public class GmallUsermanageApplication {
    public static void main(String[] args) {
        SpringApplication.run(GmallUsermanageApplication.class, args);
        System.out.println("启动成功");
    }
}
