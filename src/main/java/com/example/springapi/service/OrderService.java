package com.example.springapi.service;

import com.example.springapi.api.dto.CustomerDTO;
import com.example.springapi.api.dto.OrderDTO;
import com.example.springapi.api.dto.ProductDTO;
import com.example.springapi.persistence.entity.CustomerEntity;
import com.example.springapi.persistence.entity.OrderEntity;
import com.example.springapi.persistence.entity.ProductEntity;
import com.example.springapi.persistence.repository.CustomerRepository;
import com.example.springapi.persistence.repository.OrderRepository;
import com.example.springapi.persistence.repository.ProductRepository;
import com.example.springapi.publisher.RabbitMQProducer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private RabbitMQProducer producer;

    ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public OrderService(OrderRepository orderRepository, ProductRepository productRepository, CustomerRepository customerRepository, RabbitMQProducer producer) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.customerRepository = customerRepository;
        this.producer = producer;
    }

    public Optional<OrderDTO> getOrder(Integer id) throws JsonProcessingException {
        Optional<OrderEntity> orderFromDB = orderRepository.getOrderEntityById(id);
        Optional<OrderDTO> orderDTO = orderFromDB.map(this::orderEntityMapper);

        Optional<OrderDTO> orderDTOWithTotal = setTotal(orderDTO);

        return orderDTOWithTotal;
    }

    public List<Optional<OrderDTO>> getOrders() throws JsonProcessingException {
        List<OrderEntity> ordersFromDBList = orderRepository.findAll().stream().limit(10).collect(Collectors.toList());

        List<OrderDTO> orderDTOList = ordersFromDBList.stream()
                .map(this::orderEntityMapper)
                .collect(Collectors.toList());

        List<Optional<OrderDTO>> orderDTOListWithTotal = new ArrayList<>();
        for (OrderDTO orderDTO : orderDTOList) {
            Optional<OrderDTO> order = setTotal(Optional.of(orderDTO));
            orderDTOListWithTotal.add(order);
        }

        return orderDTOListWithTotal;
    }

    //Donne les 10 dernières commande d'un utilisateur via son id
    public List<Optional<OrderDTO>> getCustomerOrders(Integer id) throws JsonProcessingException {
        List<OrderEntity> ordersFromDBList = orderRepository.findAll().stream().filter(order -> order.getIdCustomer() == id).limit(10).collect(Collectors.toList());

        List<OrderDTO> orderDTOList = ordersFromDBList.stream()
                .map(this::orderEntityMapper)
                .collect(Collectors.toList());

        List<Optional<OrderDTO>> orderDTOListWithTotal = new ArrayList<>();
        for (OrderDTO orderDTO : orderDTOList) {
            Optional<OrderDTO> order = setTotal(Optional.of(orderDTO));
            orderDTOListWithTotal.add(order);
        }

        return orderDTOListWithTotal;
    }

    public void postOrder(OrderDTO orderDTO) {
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setId(generateId()); // Générer un nouvel identifiant
        orderEntity.setIdCustomer(orderDTO.getIdCustomer());
        orderEntity.setIdProduct(orderDTO.getIdProduct());
        orderEntity.setQuantity(orderDTO.getQuantity());

        // Récupérer le CustomerDTO correspondant à l'ID du produit
        Optional<CustomerDTO> customerDTO = getCustomer(orderDTO.getIdCustomer());

        // Vérifier si le client existe
        if (customerDTO == null) {
            throw new NullPointerException("Le client n'existe pas.");
        }

        // Récupérer le ProductDTO correspondant à l'ID du produit
        Optional<ProductDTO> productDTO = getProduct(orderDTO.getIdProduct());

        // Vérifier si le produit existe
        if (productDTO == null) {
            throw new NullPointerException("Le produit n'existe pas.");
        }

        orderRepository.save(orderEntity);
    }

    public void putOrder(Integer id, OrderDTO orderDTO) {
        Optional<OrderEntity> optionalOrderEntity = orderRepository.getOrderEntityById(id);
        optionalOrderEntity.ifPresent(orderEntity -> {
            orderEntity.setIdCustomer(orderDTO.getIdCustomer());
            orderEntity.setIdProduct(orderDTO.getIdProduct());
            orderEntity.setQuantity(orderDTO.getQuantity());
            orderRepository.save(orderEntity);
        });
    }

    public void deleteOrder(Integer id) {
        orderRepository.deleteById(id);
    }

    private OrderDTO orderEntityMapper(OrderEntity orderEntity){

        return OrderDTO.builder()
                .idCustomer(orderEntity.getIdCustomer())
                .idProduct(orderEntity.getIdProduct())
                .quantity(orderEntity.getQuantity())
                .build();
    }

    private ProductDTO productEntityMapper(ProductEntity productEntity){
        return ProductDTO.builder()
                .label(productEntity.getLabel())
                .price(productEntity.getPrice())
                .build();
    }

    private CustomerDTO customerEntityMapper(CustomerEntity customerEntity){
        return CustomerDTO.builder()
                .name(customerEntity.getName())
                .age(customerEntity.getAge())
                .email(customerEntity.getEmail())
                .build();
    }

    private int generateId() {
        return Math.toIntExact(orderRepository.count() + 1);
    }

    public Optional<ProductDTO> getProduct(Integer id) {
        Optional<ProductEntity> productFromDB = productRepository.getProductEntityById(id);

        return productFromDB.map(this::productEntityMapper);
    }

    public Optional<CustomerDTO> getCustomer(Integer id) {
        Optional<CustomerEntity> customerFromDB = customerRepository.getCustomerEntityById(id);

        return customerFromDB.map(this::customerEntityMapper);
    }

    private Optional<OrderDTO> setTotal(Optional<OrderDTO> orderDTO) throws JsonProcessingException {
        String reply = producer.sendMessageWithReturn("ACTION_GET_PRODUCT:"+orderDTO.get().getIdProduct());
        Optional<ProductDTO> productDTO = Optional.ofNullable(objectMapper.readValue(reply, ProductDTO.class));
        double total = productDTO.get().getPrice() * orderDTO.get().getQuantity();
        orderDTO.get().setTotal(total);
        return orderDTO;
    }
}
