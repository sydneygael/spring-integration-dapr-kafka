package com.example.sadjoum.config;

import com.example.sadjoum.model.Order;
import com.example.sadjoum.repository.OrderRepository;
import com.example.sadjoum.transformer.NewlineSplitter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.core.GenericTransformer;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.file.dsl.Files;
import org.springframework.integration.handler.LoggingHandler;
import org.springframework.integration.http.dsl.Http;
import org.springframework.messaging.support.ErrorMessage;

import java.io.File;

@Configuration
@EnableIntegration
@Slf4j
public class IntegrationConfig {

    // URL de l'endPoint pour les liaisons Kafka
    public static final String KAFKA_BINDINGS_URL = "http://localhost:3500/v1.0/bindings/kafka-bindings";
    @Value("${datasource.inputFolder}")
    private String inputFolder;

    @Autowired
    private OrderRepository orderRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // Configuration du flux d'intégration pour traiter les fichiers CSV
    @Bean
    public IntegrationFlow csvToDatabaseKafkaFlow() {
        return IntegrationFlow.from(Files.inboundAdapter(new File(inputFolder))
                                .preventDuplicates(true)
                                .patternFilter("*.csv"),
                        e -> e.poller(p -> p.fixedRate(1000)))
                .split(new NewlineSplitter())
                .transform(csvLineToPojoTransformer())
                .log(LoggingHandler.Level.INFO, "CustomLogger", m -> "Transformed message: " + m.getPayload())
                .<Order, String>route(Order::getType,
                        mapping -> mapping.subFlowMapping("pizza", pizzaSubFlow())
                                .subFlowMapping("pates", pastaSubFlow()))
                .get();
    }

    // Transformer une ligne CSV en objet Order
    @Bean
    public GenericTransformer<String, Order> csvLineToPojoTransformer() {
        return csvLine -> {
            String[] parts = csvLine.split(",");
            long orderId = Long.parseLong(parts[0].trim());
            String type = parts[1].trim();
            int qte = Integer.parseInt(parts[2].trim());
            int prixUnitaire = Integer.parseInt(parts[3].trim());
            int prixTotal = qte * prixUnitaire;

            return new Order(orderId, type, qte, prixUnitaire, prixTotal);
        };
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

    // Sous-flux pour le traitement des commandes de pizza
    @Bean
    public IntegrationFlow pizzaSubFlow() {
        return f -> f
                .wireTap(pizzaDbFlow()) // Enregistrement des commandes de pizza dans la base de données
                .wireTap(pizzaKafkaFlow()); // Publication des commandes de pizza sur Kafka
    }

    @Bean
    public IntegrationFlow pizzaDbFlow() {
        return f -> f
                .handle((message -> {
                    var messagePayload = (Order) message.getPayload();
                    log.info("saving pizza {} in database", messagePayload);
                    orderRepository.save(messagePayload);
                }));
    }

    // Flux pour la publication des commandes de pizza sur Kafka
    @Bean
    public IntegrationFlow pizzaKafkaFlow() {
        return f -> f
                .transform(Order.class, order -> transformOrderToKafkaEvent(order, "pizza-topic"))
                .log(LoggingHandler.Level.INFO, "CustomLogger", m -> "pizzaKafkaFlow message: " + m.getPayload())
                .handle(Http.outboundGateway("http://localhost:3500/v1.0/bindings/kafka-bindings")
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
        return f -> f
                .handle((message -> {
                    var messagePayload = (Order) message.getPayload();
                    log.info("saving pasta {} in database", messagePayload);
                    orderRepository.save(messagePayload);
                }));
    }
    // Flux pour la publication des commandes de pates sur Kafka
    @Bean
    public IntegrationFlow pastaKafkaFlow() {
        return f -> f
                .transform(Order.class, order -> transformOrderToKafkaEvent(order, "pasta-topic"))
                .log(LoggingHandler.Level.INFO, "CustomLogger", m -> "pastaKafkaFlow message: " + m.getPayload())
                .handle(Http.outboundGateway(KAFKA_BINDINGS_URL)
                        .httpMethod(HttpMethod.POST)
                        .expectedResponseType(String.class)
                        .requestFactory(requestFactory()));
    }

    private Object transformOrderToKafkaEvent(Order order, String partition) {
        // Transformer l'objet Order en JSON
        var orderJson = convertOrderToJson(order);
        // Créer le payload JSON final avec les champs partition et order
        return String.format("{\"operation\": \"create\", \"data\": {\"topic\": \"%s\", \"value\": %s}}", partition, orderJson);

    }

    private String convertOrderToJson(Order order) {
        try {
            return objectMapper.writeValueAsString(order);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Erreur lors de la conversion de l'objet Order en JSON", e);
        }
    }

}
