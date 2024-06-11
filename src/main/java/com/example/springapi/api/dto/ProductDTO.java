package com.example.springapi.api.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProductDTO {

    private String label;
    private double price;
}
