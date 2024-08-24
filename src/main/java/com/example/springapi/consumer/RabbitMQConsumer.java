package com.example.springapi.consumer;

import com.example.springapi.api.dto.OrderDTO;
import com.example.springapi.api.dto.ProductDTO;
import com.example.springapi.service.OrderService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class RabbitMQConsumer {
    private static final Logger LOGGER = LoggerFactory.getLogger(RabbitMQConsumer.class);
    private final OrderService orderService;

    public RabbitMQConsumer(OrderService orderService) {
        this.orderService = orderService;
    }

    @RabbitListener(queues = {"${rabbitmq.queue.name}"})
    public String consume(String message) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        LOGGER.info(String.format("Received message -> %s", message));
        if(message.startsWith("ACTION")){

            String[] parts = message.split(":");
            LOGGER.info(parts[0]);
            switch (parts[0]){
                //Format : //ACTION_TYPE_ACTION:ID
                //Exemple : ACTION_GET_PRODUCT:1

                //Exemple : ACTION_GET_MY_ORDERS:1 (Id du client)
                case "ACTION_GET_MY_ORDERS":
                    LOGGER.info("TESRTRTRTRTRTRTRTRTRTRTRTRTRT");
                    LOGGER.info("Get orders of customer from RabbitMQ with his id {}", parts[1]);
                    List<Optional<OrderDTO>> orderDTOList = orderService.getCustomerOrders(Integer.parseInt(parts[1]));
                    if(orderDTOList.size() > 0){
                        List<OrderDTO> orders = orderDTOList.stream()
                                .filter(Optional::isPresent) // Filtrer les Optional non vides
                                .map(Optional::get) // Extraire les objets OrderDTO
                                .collect(Collectors.toList());
                        LOGGER.info("Return product to RabbitMQ {}", orders);
                        String json = objectMapper.writeValueAsString(orders);
                        return json;
                    }
                    break;

                //Exemple : ACTION_GET_ORDER:1 (Id de la commande)
                case "ACTION_GET_ORDER":
                    LOGGER.info("Get order from RabbitMQ with id {}", parts[1]);
                    Optional<OrderDTO> orderDTO = orderService.getOrder(Integer.parseInt(parts[1]));

                    if(orderDTO.isPresent()){
                        OrderDTO order = orderDTO.get();
                        LOGGER.info("Return product to RabbitMQ {}", orderDTO);
                        String json = objectMapper.writeValueAsString(order);
                        return json;
                    }
                    break;
            }
        }
        return null;
    }
}
