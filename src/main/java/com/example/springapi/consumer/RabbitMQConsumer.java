package com.example.springapi.consumer;

import com.example.springapi.api.dto.ProductDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class RabbitMQConsumer {
    private static final Logger LOGGER = LoggerFactory.getLogger(RabbitMQConsumer.class);

    @RabbitListener(queues = {"${rabbitmq.queue.name}"})
    public void consume(String message) {
        LOGGER.info(String.format("Received message -> %s", message));
        /*if(message.startsWith("ACTION")){
            String[] parts = message.split(":");
            switch (parts[0]){
                //Format : //ACTION_TYPE_ACTION:ID
                //Exemple : ACTION_GET_PRODUCT:1
                //TODO possibilité d'ajouter la queue de retour exemple  ACTION_GET_PRODUCT:1:orders

                case "ACTION_RETURN_PRODUCT":
                    LOGGER.info("Get product from RabbitMQ with id {}", parts[1]);
                    //Optional<ProductDTO> productDTO = productService.getProduct(Integer.parseInt(parts[1]));
                    //if(productDTO.isPresent()){
                    //    LOGGER.info("Return product to RabbitMQ {}", productDTO);
                    //    producer.sendMessage("ACTION_RETURN_PRODUCT:" + productDTO);
                    //}
                    break;
            }
        }*/
    }
}
