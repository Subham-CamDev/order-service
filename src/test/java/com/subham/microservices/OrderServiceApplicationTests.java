package com.subham.microservices;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.subham.microservices.events.OrderPlacedEvent;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig;
import io.restassured.RestAssured;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;
import org.wiremock.integrations.testcontainers.WireMockContainer;

import java.util.HashMap;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class OrderServiceApplicationTests {

  @LocalServerPort
  private Integer port;

  static Network network = Network.newNetwork();

  @Container
  static WireMockContainer wireMockContainer = new WireMockContainer("wiremock/wiremock:3.13.2-2");

  @Container
  static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("apache/kafka:4.2.0"))
          .withListener("kafka:19092")
          .withNetwork(network)
          .withNetworkAliases("broker");

  @Container
  static GenericContainer<?> schemaRegistry = new GenericContainer<>("confluentinc/cp-schema-registry:8.0.4")
                  .withNetwork(network)
                  .withNetworkAliases("schema-registry")
                  .withExposedPorts(8081)
                  .dependsOn(kafka)
                  .withEnv("SCHEMA_REGISTRY_HOST_NAME", "schema-registry")
                  .withEnv("SCHEMA_REGISTRY_LISTENERS", "http://0.0.0.0:8081")
                  .withEnv("SCHEMA_REGISTRY_KAFKASTORE_BOOTSTRAP_SERVERS",
                          "PLAINTEXT://broker:19092")
                  .waitingFor(Wait.forHttp("/subjects").forStatusCode(200));

  @DynamicPropertySource
  static void configureSources(DynamicPropertyRegistry registry) {
    registry.add("service.inventory.base-url", wireMockContainer::getBaseUrl);
    registry.add("spring.kafka.bootstrap-servers",
            kafka::getBootstrapServers);
    registry.add("spring.kafka.producer.properties.schema.registry.url",
            () -> "http://" +
                    schemaRegistry.getHost() + ":" +
                    schemaRegistry.getMappedPort(8081));
  }

  @Autowired
  private KafkaTemplate<String, OrderPlacedEvent> kafkaTemplate;

  private Consumer<String, OrderPlacedEvent> consumer;

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

  @BeforeAll
  static void startSchemaRegistry() {
    kafka.start();
    schemaRegistry.start();
  }

  @Test
  void testPlaceOrderSuccess() {
    consumer = consumerFactory().createConsumer();
    consumer.subscribe(java.util.List.of("order-placed"));
    String requestBody = """
            {
                "skuCode": "samsung_f34_5g",
                "price": 16000,
                "quantity": 2,
                "userDetails": {
                    "email": "testuser@example.com"
                  }
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

    ConsumerRecord<String, OrderPlacedEvent> record =
            KafkaTestUtils.getSingleRecord(consumer, "order-placed");

    assertEquals("testuser@example.com", record.value().getEmail().toString());
  }

  @Test
  void testPlaceOrderFailed() {
    consumer = consumerFactory().createConsumer();
    consumer.subscribe(java.util.List.of("order-placed"));
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

  private ConsumerFactory<String, OrderPlacedEvent> consumerFactory() {
    Map<String, Object> props = new HashMap<>();
    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class);
    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());
    props.put(ConsumerConfig.GROUP_ID_CONFIG, "test-group");
    props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
    props.put(KafkaAvroDeserializerConfig.SCHEMA_REGISTRY_URL_CONFIG,
            "http://" + schemaRegistry.getHost() + ":" +
                    schemaRegistry.getMappedPort(8081));
    props.put("specific.avro.reader", "true");

    return new DefaultKafkaConsumerFactory<>(props);
  }

}
