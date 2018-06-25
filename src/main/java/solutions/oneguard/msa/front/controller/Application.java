package solutions.oneguard.msa.front.controller;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import solutions.oneguard.msa.core.messaging.MessageConsumerConfiguration;

@SpringBootApplication
@Configuration
public class Application {
    public static void main(String... args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public MessageConsumerConfiguration messageConsumerConfiguration(MicroServiceResponseHandler handler) {
        return new MessageConsumerConfiguration()
            .setDefaultHandler(handler);
    }
}
