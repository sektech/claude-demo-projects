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
    void getById_found() throws Exception {
        Product saved = repository.save(new Product("Keyboard", "Mechanical keyboard", 79.99, 10));
        mockMvc.perform(get("/api/products/" + saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saved.getId()))
                .andExpect(jsonPath("$.name").value("Keyboard"));
    }

    @Test
    void getById_notFound() throws Exception {
        mockMvc.perform(get("/api/products/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createProduct_validationError() throws Exception {
        Product p = new Product("", "Missing name", -5.0, -1);
        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(p)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.name").exists())
                .andExpect(jsonPath("$.errors.price").exists())
                .andExpect(jsonPath("$.errors.quantity").exists());
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
    void updateProduct_validationError() throws Exception {
        Product saved = repository.save(new Product("Headset", "Wired headset", 59.99, 15));
        saved.setName("");
        mockMvc.perform(put("/api/products/" + saved.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(saved)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.name").exists());
    }

    @Test
    void updateProduct_notFound() throws Exception {
        Product p = new Product("Headset", "Wired headset", 59.99, 15);
        mockMvc.perform(put("/api/products/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(p)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteProduct() throws Exception {
        Product saved = repository.save(new Product("Webcam", "HD webcam", 89.99, 8));
        mockMvc.perform(delete("/api/products/" + saved.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteProduct_notFound() throws Exception {
        mockMvc.perform(delete("/api/products/999"))
                .andExpect(status().isNotFound());
    }
}
