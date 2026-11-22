package com.example.banking.bank;

import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.apache.kafka.clients.admin.NewTopic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.util.backoff.FixedBackOff;
import tools.jackson.databind.ObjectMapper;

@Configuration(proxyBeanMethods = false)
@EnableScheduling
class BankActivationMessaging {
    private static final Logger log = LoggerFactory.getLogger(BankActivationMessaging.class);

    private final ActivationOutboxRepository outbox;
    private final BankActivationService activation;
    private final KafkaTemplate<String, String> kafka;
    private final ObjectMapper json;
    private final String requestTopic;
    private final String resultTopic;

    BankActivationMessaging(ActivationOutboxRepository outbox, BankActivationService activation,
                            KafkaTemplate<String, String> kafka, ObjectMapper json,
                            @Value("${banking.kafka.request-topic}") String requestTopic,
                            @Value("${banking.kafka.result-topic}") String resultTopic) {
        this.outbox = outbox;
        this.activation = activation;
        this.kafka = kafka;
        this.json = json;
        this.requestTopic = requestTopic;
        this.resultTopic = resultTopic;
    }

    @Bean
    NewTopic activationRequests() {
        return TopicBuilder.name(requestTopic).partitions(1).replicas(1).build();
    }

    @Bean
    NewTopic activationResults() {
        return TopicBuilder.name(resultTopic).partitions(1).replicas(1).build();
    }

    @Bean
    DefaultErrorHandler activationErrorHandler() {
        var handler = new DefaultErrorHandler(new FixedBackOff(1000, FixedBackOff.UNLIMITED_ATTEMPTS));
        handler.addNotRetryableExceptions(InvalidActivationRequest.class);
        return handler;
    }

    @Scheduled(fixedDelayString = "${banking.kafka.poll-delay-ms:1000}")
    void publishPendingEvents() {
        for (var event : outbox.findTop20ByOrderByIdAsc()) {
            try {
                var topic = event.outcome == null ? requestTopic : resultTopic;
                var payload = event.outcome == null
                    ? json.writeValueAsString(new ActivationRequest(event.customerId, event.requestId))
                    : json.writeValueAsString(new ActivationResult(event.customerId, event.requestId, event.outcome));
                kafka.send(topic, event.customerId.toString(), payload).get(15, TimeUnit.SECONDS);
                outbox.deleteById(event.id);
            } catch (InterruptedException interrupted) {
                Thread.currentThread().interrupt();
                return;
            } catch (Exception failure) {
                log.warn("Bank activation event {} is still queued for publication", event.id, failure);
                return;
            }
        }
    }

    @KafkaListener(topics = "${banking.kafka.request-topic}")
    void consumeRequest(String payload) {
        ActivationRequest request;
        try {
            request = json.readValue(payload, ActivationRequest.class);
        } catch (tools.jackson.core.JacksonException invalid) {
            throw new InvalidActivationRequest();
        }
        if (request == null || request.customerId() <= 0 || request.requestId() == null) {
            throw new InvalidActivationRequest();
        }
        activation.activate(request.customerId(), request.requestId());
    }

    record ActivationRequest(long customerId, UUID requestId) {}
    record ActivationResult(long customerId, UUID requestId, ActivationStatus status) {}

    private static class InvalidActivationRequest extends RuntimeException {
        InvalidActivationRequest() {
            super("Invalid bank activation request");
        }
    }
}
