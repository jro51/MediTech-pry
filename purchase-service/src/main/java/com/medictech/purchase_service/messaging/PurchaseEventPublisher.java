package com.medictech.purchase_service.messaging;

import com.medictech.purchase_service.dto.PurchaseEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PurchaseEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String TOPIC = "purchase-events";

    public void publishPurchaseCreated(PurchaseEvent event) {
        log.info("Publicando evento de compra en Kafka. Topic: {}, userId: {}",
                TOPIC, event.getUserId());
        kafkaTemplate.send(TOPIC, String.valueOf(event.getPurchaseId()), event);
    }
}
