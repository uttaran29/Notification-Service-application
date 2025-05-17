package com.notificationapp.notification.listener;

import com.notificationapp.notification.config.RabbitMQConfig;
import com.notificationapp.notification.models.Notification;
import com.notificationapp.notification.service.NotificationService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class NotificationListener {
    @Autowired
    private NotificationService notificationService;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
    public void consume(Notification notification) {
        notificationService.processNotification(notification);
    }
}
