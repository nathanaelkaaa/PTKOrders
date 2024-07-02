package com.example.springapi.api.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProductDTO {
    private String label;
    private double price;
    private int quantity;

    @JsonCreator(mode = JsonCreator.Mode.DEFAULT)
    public ProductDTO(@JsonProperty("label") String label,
                      @JsonProperty("price") double price,
                      @JsonProperty("quantity") int quantity) {
        this.label = label;
        this.price = price;
        this.quantity = quantity;
    }
}
