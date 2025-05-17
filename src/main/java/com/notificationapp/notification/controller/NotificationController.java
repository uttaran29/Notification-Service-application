package com.notificationapp.notification.controller;

import com.notificationapp.notification.dto.NotificationRequest;
import com.notificationapp.notification.models.Notification;
import com.notificationapp.notification.models.User;
import com.notificationapp.notification.service.NotificationService;
import com.notificationapp.notification.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserService userService;

    @PostMapping("/notifications")
    public ResponseEntity<?> sendNotification(@RequestBody NotificationRequest notificationRequest) {
        Optional<User> userOptional = userService.getById(notificationRequest.getUserId());

        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User not found");
        }

        String response = String.valueOf(notificationService.sendNotification(
                userOptional.get(),
                notificationRequest.getType(),
                notificationRequest.getMessage()
        ));

        return ResponseEntity.ok(response);
    }

    @GetMapping("/users/{id}/notifications")
    public ResponseEntity<List<Notification>> getUserNotifications(@PathVariable Long id) {
        return userService.getById(id)
                .map(user -> ResponseEntity.ok(notificationService.getUserNotifications(user)))
                .orElse(ResponseEntity.notFound().build());
    }

}
