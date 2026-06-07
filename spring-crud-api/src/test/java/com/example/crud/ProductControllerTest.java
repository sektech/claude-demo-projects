package com.example.crud;

import com.example.crud.model.Product;
import com.example.crud.repository.ProductRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ProductControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ProductRepository repository;
    @Autowired ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }

    @Test
    void createProduct() throws Exception {
        Product p = new Product("Monitor", "27-inch 4K", 399.99, 5);
        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(p)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Monitor"));
    }

    @Test
    void getAll() throws Exception {
        repository.save(new Product("Speaker", "Bluetooth speaker", 49.99, 20));
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getById_notFound() throws Exception {
        mockMvc.perform(get("/api/products/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateProduct() throws Exception {
        Product saved = repository.save(new Product("Headset", "Wired headset", 59.99, 15));
        saved.setPrice(49.99);
        mockMvc.perform(put("/api/products/" + saved.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(saved)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.price").value(49.99));
    }

    @Test
    void deleteProduct() throws Exception {
        Product saved = repository.save(new Product("Webcam", "HD webcam", 89.99, 8));
        mockMvc.perform(delete("/api/products/" + saved.getId()))
                .andExpect(status().isNoContent());
    }
}
