package com.digitalwallet.notificationservice.service;

import com.digitalwallet.notificationservice.dto.NotificationResponse;
import com.digitalwallet.notificationservice.entity.Notification;
import com.digitalwallet.notificationservice.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    public List<NotificationResponse> getNotificationsByUserId(Long userId) {
        List<Notification> notifications = notificationRepository.findByUserIdOrderByTimestampDesc(userId);
        return notifications.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public boolean deleteNotification(Long notificationId, Long userId) {
        try {
            notificationRepository.deleteByIdAndUserId(notificationId, userId);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private NotificationResponse convertToResponse(Notification notification) {
        NotificationResponse response = new NotificationResponse();
        response.setId(notification.getId());
        response.setType(notification.getType());
        response.setMessage(notification.getMessage());
        response.setReferenceId(notification.getReferenceId());
        response.setTimestamp(notification.getTimestamp());
        return response;
    }
}