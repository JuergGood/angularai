package ch.goodone.angularai.backend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    @Value("${spring.mail.from}")
    private String fromEmail;

    @Value("${app.base-url}")
    private String baseUrl;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendVerificationEmail(String toEmail, String token) {
        String verificationUrl = baseUrl + "/api/auth/verify?token=" + token;
        
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Verify your GoodOne account");
        message.setText("Thank you for registering. Please click the link below to verify your account:\n\n" + verificationUrl);

        try {
            mailSender.send(message);
            logger.info("Verification email sent to {}", toEmail);
        } catch (Exception e) {
            logger.error("Failed to send verification email to {}", toEmail, e);
        }
    }
}
