package com.moon.rpc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * @author mzx
 * @date 2022/7/15 16:38
 */
@SpringBootApplication
@ComponentScan("com.moon.rpc")
public class ProviderAppLoader {
    public static void main(String[] args) {
        SpringApplication.run(ProviderAppLoader.class, args);
    }
}
