package com.huwei.gmall0416.llist;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("com.huwei.gmall0416")
public class GmallListWebApplication {
    public static void main(String[] args) {
        SpringApplication.run(GmallListWebApplication.class, args);
    }
}
