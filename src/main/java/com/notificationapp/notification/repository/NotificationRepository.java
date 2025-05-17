package com.notificationapp.notification.repository;

import com.notificationapp.notification.models.Notification;
import com.notificationapp.notification.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUser(User user);
}
