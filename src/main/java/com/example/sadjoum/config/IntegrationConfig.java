package com.example.sadjoum.config;

import com.example.sadjoum.model.Order;
import com.example.sadjoum.transformer.CsvToPojoTransformer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.core.GenericTransformer;
import org.springframework.integration.dsl.ConsumerEndpointSpec;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.file.dsl.Files;
import org.springframework.integration.http.dsl.Http;
import org.springframework.integration.jpa.dsl.Jpa;
import org.springframework.integration.jpa.dsl.JpaUpdatingOutboundEndpointSpec;
import org.springframework.integration.jpa.support.PersistMode;
import org.springframework.messaging.support.ErrorMessage;
import org.springframework.messaging.support.MessageBuilder;

import javax.sql.DataSource;
import java.io.File;
import java.util.UUID;

@Configuration
@EnableIntegration
public class IntegrationConfig {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private CsvToPojoTransformer csvToPojoTransformer;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Value("${datasource.inputFolder}")
    private String inputFolder;

    private ObjectMapper objectMapper = new ObjectMapper();


    @Bean
    public IntegrationFlow csvToDatabaseKafkaFlow() {
        return IntegrationFlow.from(Files.inboundAdapter(new File(inputFolder))
                                .preventDuplicates(true)
                                .patternFilter("*.csv"),
                        e -> e.poller(p -> p.fixedRate(1000)))
                .split(Files.splitter().markers())
                .transform(csvToPojoTransformer)
                .<Order, String>route(Order::type,
                        mapping -> mapping.subFlowMapping("pizza", pizzaSubFlow())
                                .subFlowMapping("pates", pastaSubFlow()))
                .get();
    }

    @Bean
    public IntegrationFlow errorHandlingFlow() {
        return IntegrationFlow.from("errorChannel")
                .handle((GenericTransformer<ErrorMessage, String>) (errorMessage) -> {
                    Throwable throwable = errorMessage.getPayload().getCause();
                    // Log or handle the error as needed
                    System.out.println("Error occurred: " + throwable.getMessage());
                    return null;
                })
                .get();
    }

    @Bean
    public IntegrationFlow sendErrorToDapr(String errorMessage) {
        return IntegrationFlow.from(errorHandlingFlow())
                .transform(message -> MessageBuilder.withPayload("{\"message\":\"" + errorMessage + "\"}"))
                .handle(Http.outboundGateway("http://localhost:3500/v1/pubsub/error-topic/publish")
                        .httpMethod(HttpMethod.POST)
                        .expectedResponseType(String.class))
                .get();
    }

    @Bean
    public IntegrationFlow pizzaSubFlow() {
        return f -> f
                .wireTap(pizzaDbFlow())
                .wireTap(pizzaKafkaFlow());
    }

    @Bean
    public IntegrationFlow pizzaDbFlow() {
        return f -> f
                .handle(getJpaOutboundEndpointSpec(),
                        ConsumerEndpointSpec::transactional);
    }

    private JpaUpdatingOutboundEndpointSpec getJpaOutboundEndpointSpec() {
        return Jpa.outboundAdapter(this.entityManagerFactory)
                .entityClass(Order.class)
                .persistMode(PersistMode.PERSIST);
    }

    @Bean
    public IntegrationFlow pizzaKafkaFlow() {
        return f -> f
                .transform(Order.class, this::transformOrderToKafkaEvent)
                .handle(Http.outboundGateway("http://localhost:3500/v1/pubsub/pizza-topic/publish")
                        .httpMethod(HttpMethod.POST)
                        .expectedResponseType(String.class)
                        .requestFactory(requestFactory()));
    }

    private SimpleClientHttpRequestFactory requestFactory() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setOutputStreaming(false); // Set to true if needed
        return requestFactory;
    }

    @Bean
    public IntegrationFlow pastaSubFlow() {
        return f -> f
                .wireTap(pastaDbFlow())
                .wireTap(pastaKafkaFlow());
    }

    @Bean
    public IntegrationFlow pastaDbFlow() {
        return f -> f.handle(getJpaOutboundEndpointSpec(),
                ConsumerEndpointSpec::transactional);
    }

    @Bean
    public IntegrationFlow pastaKafkaFlow() {
        return f -> f
                .transform(Order.class, this::transformOrderToKafkaEvent)
                .handle(Http.outboundGateway("http://localhost:3500/v1/pubsub/pasta-topic/publish")
                        .httpMethod(HttpMethod.POST)
                        .expectedResponseType(String.class)
                        .requestFactory(requestFactory()));
    }

    private Object transformOrderToKafkaEvent(Order order) {
        // Transformer l'objet Order en JSON
        String jsonPayload = convertOrderToJson(order);
        // Générer un UID aléatoire pour l'eventId
        String eventId = UUID.randomUUID().toString();
        // Créer le payload JSON final avec les champs eventId et data
        return String.format("{\"eventId\": \"%s\", \"data\": %s}", eventId, jsonPayload);
    }

    private String convertOrderToJson(Order order) {
        try {
            return objectMapper.writeValueAsString(order);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Erreur lors de la conversion de l'objet Order en JSON", e);
        }
    }

}
