package com.share.GroupPurchasing;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
@MapperScan("com.pilates.webserver.db")
public class APP {

    public static void main(String[] args) {
        SpringApplication.run(APP.class, args);
    }

}
