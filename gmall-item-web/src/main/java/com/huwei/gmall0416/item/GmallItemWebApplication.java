package com.huwei.gmall0416.item;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages="com.huwei.gmall0416")
public class GmallItemWebApplication {

    public static void main(String[] args) {
        SpringApplication.run(GmallItemWebApplication.class, args);

    }
}
