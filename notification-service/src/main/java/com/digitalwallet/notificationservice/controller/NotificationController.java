package com.digitalwallet.notificationservice.controller;

import com.digitalwallet.notificationservice.dto.NotificationResponse;
import com.digitalwallet.notificationservice.service.NotificationService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notification")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<NotificationResponse>> getUserNotifications(
            @PathVariable Long userId,
            HttpServletRequest request) {

        Long authenticatedUserId = (Long) request.getAttribute("authenticatedUserId");
        if (!userId.equals(authenticatedUserId)) {
            return ResponseEntity.status(403).build();
        }

        List<NotificationResponse> notifications = notificationService.getNotificationsByUserId(userId);
        return ResponseEntity.ok(notifications);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteNotification(
            @PathVariable Long id,
            @RequestParam Long userId,
            HttpServletRequest request) {

        Long authenticatedUserId = (Long) request.getAttribute("authenticatedUserId");
        if (!userId.equals(authenticatedUserId)) {
            return ResponseEntity.status(403).body("Access denied");
        }

        boolean deleted = notificationService.deleteNotification(id, userId);
        if (deleted) {
            return ResponseEntity.ok("Notification deleted successfully");
        } else {
            return ResponseEntity.badRequest().body("Failed to delete notification");
        }
    }
}