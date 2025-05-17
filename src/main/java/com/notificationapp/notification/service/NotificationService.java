package com.notificationapp.notification.service;

import com.notificationapp.notification.models.Notification;
import com.notificationapp.notification.models.User;
import com.notificationapp.notification.publisher.NotificationPublisher;
import com.notificationapp.notification.repository.NotificationRepository;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private NotificationPublisher notificationPublisher;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${twilio.phone-number}")
    private String twilioPhoneNumber;

    private static final int MAX_RETRIES = 3;

    public Notification sendNotification(User user, String type, String message) {
        if (user == null || type == null || message == null || message.trim().isEmpty()) {
            throw new IllegalArgumentException("Invalid input for notification");
        }

        Notification notification = Notification.builder()
                .user(user)
                .type(type.toUpperCase())
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();

        Notification savedNotification = notificationRepository.save(notification);
        notificationPublisher.publish(savedNotification);

        return savedNotification;
    }

    public void processNotification(Notification notification) {
        int attempts = 0;
        boolean success = false;

        while (attempts < MAX_RETRIES && !success) {
            try {
                String type = notification.getType().toUpperCase();

                switch (type) {
                    case "EMAIL":
                        String email = notification.getUser().getEmail();
                        if (email == null || email.isEmpty()) throw new IllegalArgumentException("User email is missing");
                        sendEmail(email, notification.getMessage());
                        break;

                    case "SMS":
                        String phone = notification.getUser().getMobile();
                        if (phone == null || phone.isEmpty()) throw new IllegalArgumentException("User mobile is missing");
                        sendSMS(phone, notification.getMessage());
                        break;

                    default:
                        throw new UnsupportedOperationException("Unsupported notification type: " + type);
                }

                notificationRepository.save(notification); // Save after successful delivery
                success = true;

            } catch (Exception e) {
                attempts++;
                System.err.println("Retry " + attempts + "/" + MAX_RETRIES + " failed: " + e.getMessage());

                try {
                    Thread.sleep(2000L * attempts); // Exponential backoff
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }

        if (!success) {
            // Optionally push to DLQ or log critically
            System.err.println("Notification delivery failed after " + MAX_RETRIES + " attempts.");
        }
    }

    public List<Notification> getUserNotifications(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User must not be null");
        }
        return notificationRepository.findByUser(user);
    }

    private void sendEmail(String toEmail, String content) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
            helper.setTo(toEmail);
            helper.setSubject("Notification");
            helper.setText(content, true);
            helper.setFrom(fromEmail);
            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send email to " + toEmail, e);
        }
    }

    private void sendSMS(String toPhone, String content) {
        try {
            Message.creator(
                    new com.twilio.type.PhoneNumber(toPhone),
                    new com.twilio.type.PhoneNumber(twilioPhoneNumber),
                    content
            ).create();
        } catch (Exception e) {
            throw new RuntimeException("Failed to send SMS to " + toPhone, e);
        }
    }
}