package com.px3j.lush.illustrator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan( {
        "com.px3j.lush.core.api",
        "com.px3j.lush.core.reactive",
        "com.px3j.lush.illustrator"
})
public class LushTestApplication {

    public static void main(String[] args) {
        SpringApplication.run(LushTestApplication.class, args);
    }

}
