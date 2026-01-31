package ch.goodone.angularai.backend.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

@Configuration
public class MailConfig {

    @Bean
    @ConditionalOnMissingBean(JavaMailSender.class)
    public JavaMailSender javaMailSender() {
        // Return a dummy implementation if no mail sender is configured
        // This prevents the application from failing to start in environments
        // where email functionality is not needed or configured yet.
        return new JavaMailSenderImpl();
    }
}
