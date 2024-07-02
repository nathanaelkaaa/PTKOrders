package com.example.springapi.publisher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


@Service
public class RabbitMQProducer {

    @Value("${rabbitmq.exchange.products.name}")
    private String exchange;

    @Value("${rabbitmq.routing.products.key}")
    private String routingKey;

    private static final Logger LOGGER = LoggerFactory.getLogger(RabbitMQProducer.class);

    private RabbitTemplate rabbitTemplate;

    public RabbitMQProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendMessage(String message) {
        LOGGER.info(String.format("Sending message -> %s", message));
        rabbitTemplate.convertAndSend(exchange, routingKey, message);

    }

    public String sendMessageWithReturn(String message) {
        try {
            LOGGER.info(String.format("Sending message -> %s", message));
            String reply = (String) rabbitTemplate.convertSendAndReceive(exchange, routingKey, message);
            if (reply != null) {
                LOGGER.info(String.format("Received reply -> %s", reply));
                return reply;
            } else {
                LOGGER.warn("No reply received");
                return "No reply received";
            }
        } catch (Exception e) {
            LOGGER.error("Error sending message", e);
            return "Error sending message";
        }
    }

}