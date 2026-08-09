package com.unisal.processoseletivoapi.config;

import java.util.Map;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {


    @Bean
    public DirectExchange exchange(){
        return new DirectExchange("email.exchange");
    }

    @Bean
    public Queue queue(){
        return new Queue("email.confirmacao", true, false, false, Map.of("x-dead-letter-routing-key", "email.confirmacao.dlq.key",
                                                                                                            "x-dead-letter-exchange", "email.exchange"));
    }
    @Bean
    public Queue queueDlq(){
        return new Queue("email.confirmacao.dlq", true, false, false, null);
    }


    @Bean
    public Binding binding(Queue queue, DirectExchange exchange){
        return BindingBuilder.bind(queue).to(exchange).with("email.confirmacao.key");
    }
    
    @Bean
    public Binding bindingDlq(Queue queueDlq, DirectExchange exchange){
        return BindingBuilder.bind(queueDlq).to(exchange).with("email.confirmacao.dlq.key");
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
