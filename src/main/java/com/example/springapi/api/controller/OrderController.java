package com.example.springapi.api.controller;

import com.example.springapi.api.dto.OrderDTO;
import com.example.springapi.publisher.RabbitMQProducer;
import com.example.springapi.service.OrderService;
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


    @GetMapping("/{id}")
    public ResponseEntity<OrderDTO> getOrder(@PathVariable int id){
        Optional<OrderDTO> order = orderService.getOrder(id);

        return order.map(orderDTO -> new ResponseEntity<>(orderDTO, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping
    public ResponseEntity<List<OrderDTO>> getOrders(){
        List<OrderDTO> orders = orderService.getOrders();

        if (!orders.isEmpty()) {
            return new ResponseEntity<>(orders, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping
    public ResponseEntity<Void> postOrder(@RequestBody OrderDTO orderDTO) {
        orderService.postOrder(orderDTO);
        return new ResponseEntity<>(HttpStatus.CREATED);
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
