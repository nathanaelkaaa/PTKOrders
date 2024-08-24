package com.example.springapi.persistence.entity;

import com.example.springapi.api.dto.OrderDTO;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "orders")
public class OrderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private int idCustomer;
    private int idProduct;
    private int quantity;

}
