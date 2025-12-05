package com.digitalwallet.transactionservice.kafka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class PaymentEventProducer {

    private static final String TOPIC = "payment-events";

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    // ACCEPTS 5 PARAMETERS
    public void sendPaymentEvent(Long transactionId, Long senderId, Long receiverId,
                                 String amount, String referenceId) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", "payment.sent");
        event.put("transactionId", transactionId);
        event.put("senderId", senderId);
        event.put("receiverId", receiverId);
        event.put("amount", amount);
        event.put("referenceId", referenceId);
        event.put("timestamp", System.currentTimeMillis());

        kafkaTemplate.send(TOPIC, event);
        System.out.println(" Kafka event sent: " + event);
    }
}