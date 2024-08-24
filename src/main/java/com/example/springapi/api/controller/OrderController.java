package com.example.springapi.api.controller;

import com.example.springapi.api.dto.OrderDTO;
import com.example.springapi.api.dto.ProductDTO;
import com.example.springapi.publisher.RabbitMQProducer;
import com.example.springapi.service.OrderService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;
    private RabbitMQProducer producer;

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderController.class);

    @Autowired
    public OrderController(OrderService orderService,RabbitMQProducer producer){
        this.orderService = orderService;
        this.producer = producer;
    }

    @GetMapping("/publish")
    public ResponseEntity<String> sendMessage(@RequestParam("message") String message){
        producer.sendMessage(message);
        return ResponseEntity.ok("Message sent to RabbitMQ ...");
    }

    //TODO à supprimer ulterieurement
    @GetMapping("/test1")
    public ResponseEntity<String> test1(@RequestParam("id") int id) throws JsonProcessingException {
        Optional<OrderDTO> order = orderService.getOrder(id);
        //orderService.getProduct(order);
        //LOGGER.info(String.format("test1 -> %s", "Message envoyé"));
        return ResponseEntity.ok("Retour reçu : "+order);
    }

    /*
    public ResponseEntity<String> getProduct(@RequestParam("id") String id){
        LOGGER.info(String.format("test1 -> %s", "Message envoyé"));
        String reply = producer.sendMessageWithReturn("ACTION_GET_PRODUCT:"+id);
        LOGGER.info(String.format("test1 -> %s", "Message envoyé"));
        //attendre retour et récupéré Product
        
        return ResponseEntity.ok("Retour reçu : "+reply);
    }*/


    @GetMapping("/{id}")
    public ResponseEntity<OrderDTO> getOrder(@PathVariable int id) throws JsonProcessingException {
        Optional<OrderDTO> order = orderService.getOrder(id);

        return order.map(orderDTO -> new ResponseEntity<>(orderDTO, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping
    public ResponseEntity<List<Optional<OrderDTO>>> getOrders() throws JsonProcessingException {
        List<Optional<OrderDTO>> orders = orderService.getOrders();

        if (!orders.isEmpty()) {
            return new ResponseEntity<>(orders, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping
    public ResponseEntity<Void> postOrder(@RequestBody OrderDTO orderDTO) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        LOGGER.info(String.format("La commande -> %s", orderDTO));
        String reply = producer.sendMessageWithReturn("ACTION_GET_PRODUCT:"+orderDTO.getIdProduct());
        ProductDTO productDTO = objectMapper.readValue(reply, ProductDTO.class);
        if (orderDTO.getQuantity() <= productDTO.getQuantity()) {
            LOGGER.info(String.format("Creation Commande -> %s", orderDTO));
            double total = productDTO.getPrice() * orderDTO.getQuantity();
            orderDTO.setTotal(total);
            orderService.postOrder(orderDTO);
            producer.sendMessage("ACTION_BUY_PRODUCT:"+orderDTO.getIdProduct()+":"+orderDTO.getQuantity());
            return new ResponseEntity<>(HttpStatus.CREATED);
        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> putOrder(@PathVariable int id, @RequestBody OrderDTO orderDTO) {
        orderService.putOrder(id, orderDTO);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable int id) {
        orderService.deleteOrder(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}
