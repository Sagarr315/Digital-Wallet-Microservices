package com.digitalwallet.notificationservice.kafka;

import com.digitalwallet.notificationservice.entity.Notification;
import com.digitalwallet.notificationservice.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Service
public class PaymentEventConsumer {

    @Autowired
    private NotificationRepository notificationRepository;

    @KafkaListener(topics = "payment-events", groupId = "notification-group")
    public void consumePaymentEvent(Map<String, Object> eventMap) {
        System.out.println("Received Kafka event: " + eventMap);

        if ("payment.sent".equals(eventMap.get("eventType"))) {
            // Extract values from Map
            Long senderId = ((Number) eventMap.get("senderId")).longValue();
            Long receiverId = ((Number) eventMap.get("receiverId")).longValue();
            BigDecimal amount = new BigDecimal(eventMap.get("amount").toString());
            String referenceId = (String) eventMap.get("referenceId");
            Long transactionId = ((Number) eventMap.get("transactionId")).longValue();

            // Sender notification
            Notification senderNotif = new Notification();
            senderNotif.setUserId(senderId);
            senderNotif.setType("PAYMENT_SENT");
            senderNotif.setMessage("You sent ₹" + amount + " to User " + receiverId);
            senderNotif.setReferenceId(referenceId);
            senderNotif.setTimestamp(LocalDateTime.now());
            notificationRepository.save(senderNotif);

            // Receiver notification
            Notification receiverNotif = new Notification();
            receiverNotif.setUserId(receiverId);
            receiverNotif.setType("PAYMENT_RECEIVED");
            receiverNotif.setMessage("You received ₹" + amount + " from User " + senderId);
            receiverNotif.setReferenceId(referenceId);
            receiverNotif.setTimestamp(LocalDateTime.now());
            notificationRepository.save(receiverNotif);

            System.out.println("Created notifications for transaction: " + referenceId);
        }
    }
}