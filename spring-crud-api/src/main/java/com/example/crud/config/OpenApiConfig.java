package com.example.crud.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI productApiInfo() {
        return new OpenAPI()
                .info(new Info()
                        .title("Product Management API")
                        .description("REST API for managing products - create, read, update, and delete.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("API Support")
                                .email("techsek1484@gmail.com")));
    }
}
