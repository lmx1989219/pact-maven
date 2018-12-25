package com.lmx.pactdemoprovider;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

@SpringBootApplication
public class PactDemoProviderApplication {

    /**
     * set jsonConvert to highest priority
     *
     * @return
     */
    @Bean
    @Order(1)
    public MappingJackson2HttpMessageConverter newJsonConvert() {
        return new MappingJackson2HttpMessageConverter(new ObjectMapper());
    }

    public static void main(String[] args) {
        SpringApplication.run(PactDemoProviderApplication.class, args);
    }

}

