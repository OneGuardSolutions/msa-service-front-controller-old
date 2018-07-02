/*
 * This file is part of the OneGuard Micro-Service Architecture Front Controller service.
 *
 * (c) OneGuard <contact@oneguard.email>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

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
