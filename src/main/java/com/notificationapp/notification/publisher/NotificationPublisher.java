package com.notificationapp.notification.publisher;

import com.notificationapp.notification.config.RabbitMQConfig;
import com.notificationapp.notification.models.Notification;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class NotificationPublisher {
    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void publish(Notification notification) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, RabbitMQConfig.ROUTING_KEY, notification);
    }
}
