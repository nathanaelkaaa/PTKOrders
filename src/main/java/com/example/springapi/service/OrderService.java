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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;

    @Autowired
    public OrderService(OrderRepository orderRepository, ProductRepository productRepository, CustomerRepository customerRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.customerRepository = customerRepository;
    }

    public Optional<OrderDTO> getOrder(Integer id) {
        Optional<OrderEntity> orderFromDB = orderRepository.getOrderEntityById(id);
        Optional<OrderDTO> orderDTO = orderFromDB.map(this::orderEntityMapper);

        Optional<OrderDTO> orderDTOWithTotal = setTotal(orderDTO);

        return orderDTOWithTotal;
    }

    public List<OrderDTO> getOrders() {
        List<OrderEntity> ordersFromDB = orderRepository.findAll().stream().limit(10).collect(Collectors.toList());

        return ordersFromDB.stream().map(this::orderEntityMapper).collect(Collectors.toList());
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

    private Optional<OrderDTO> setTotal(Optional<OrderDTO> orderDTO) {

        Optional<ProductDTO> productDTO = getProduct(orderDTO.get().getIdProduct());
        double price = productDTO.get().getPrice();
        double total = price * orderDTO.get().getTotal();
        System.out.println(total);
        orderDTO.get().setTotal(total);

        return orderDTO;
    }
}
