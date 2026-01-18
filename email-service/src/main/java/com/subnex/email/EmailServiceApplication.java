package com.subnex.email;

import com.subnex.email.config.DotenvInitializer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
@EnableKafka
public class EmailServiceApplication {
    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(EmailServiceApplication.class);
        app.addInitializers(new DotenvInitializer());
        app.run(args);
    }
}
