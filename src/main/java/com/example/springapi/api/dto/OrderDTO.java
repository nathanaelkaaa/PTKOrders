package com.example.springapi.api.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrderDTO {

    private int idCustomer;
    private int idProduct;
    private int quantity;
    private double total;
}
