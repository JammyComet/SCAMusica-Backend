package com.musica.api_gateway;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ApiGatewayApplicationTests {
    @org.junit.jupiter.api.BeforeAll static void setup(){
        System.setProperty("scamusic.jwt.secret","clave-solo-para-pruebas-unitarias-12345678901234567890");
    }
    @org.junit.jupiter.api.AfterAll static void cleanup(){System.clearProperty("scamusic.jwt.secret");}


	@Test
	void contextLoads() {
	}

}
