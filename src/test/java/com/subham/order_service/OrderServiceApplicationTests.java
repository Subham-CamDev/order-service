package com.subham.order_service;

import com.github.tomakehurst.wiremock.client.WireMock;
import io.restassured.RestAssured;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.wiremock.integrations.testcontainers.WireMockContainer;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class OrderServiceApplicationTests {

  @LocalServerPort
  private Integer port;

  @Container
  static WireMockContainer wireMockContainer = new WireMockContainer("wiremock/wiremock:3.13.2-2");

  @DynamicPropertySource
  static void configureFeign(DynamicPropertyRegistry registry) {
    registry.add("service.inventory.base-url", wireMockContainer::getBaseUrl);
  }

  @BeforeEach
  void init() {
    RestAssured.baseURI = "http://localhost";
    RestAssured.port = port;

    configureFor(
            wireMockContainer.getHost(),
            wireMockContainer.getFirstMappedPort()
    );

    WireMock.reset();   //prevents stub leakage between tests
  }

  @Test
  void testPlaceOrderSuccess() {
    System.out.println("Wiremock URL = " + wireMockContainer.getBaseUrl());
    String requestBody = """
            {
                "skuCode": "samsung_f34_5g",
                "price": 16000,
                "quantity": 2
            }
            """;

    stubFor(get(urlPathEqualTo("/api/inventory/check"))
            .withQueryParam("skuCode", equalTo("samsung_f34_5g"))
            .willReturn(aResponse()
                    .withHeader("Content-Type", "application/json")
                    .withBody("true")));

    RestAssured.given()
            .contentType("application/json")
            .body(requestBody)
            .when()
            .post("/api/order")
            .then()
            .statusCode(201)
            .body(Matchers.equalTo("Order Placed Successfully"));
  }

  @Test
  void testPlaceOrderFailed() {
    String requestBody = """
            {
                "skuCode": "samsung",
                "price": 16000,
                "quantity": 2
            }
            """;

    stubFor(get(urlPathEqualTo("/api/inventory/check"))
            .withQueryParam("skuCode", equalTo("samsung"))
            .willReturn(aResponse()
                    .withHeader("Content-Type", "application/json")
                    .withBody("false")));

    RestAssured.given()
            .contentType("application/json")
            .body(requestBody)
            .when()
            .post("/api/order")
            .then()
            .statusCode(500)
            .body(Matchers.equalTo("Error"));
  }

}
