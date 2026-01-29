package ch.goodone.angularai.backend.service;

import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class EmailServiceTest {

    private EmailService emailService;
    private JavaMailSender mailSender;
    private MimeMessage mimeMessage;

    @BeforeEach
    void setUp() {
        mailSender = mock(JavaMailSender.class);
        mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        emailService = new EmailService(mailSender);
        ReflectionTestUtils.setField(emailService, "fromEmail", "noreply@goodone.ch");
        ReflectionTestUtils.setField(emailService, "baseUrl", "http://localhost:4200");
    }

    @Test
    void testLocalesAndSave() throws Exception {
        String baseUrl = "http://localhost:4200/api/auth/verify?token=test-token";
        
        // English
        String enHtml = (String) ReflectionTestUtils.invokeMethod(emailService, "getEmailHtml", baseUrl, false, true);
        saveEmailToFile("verification-en.html", enHtml);
        assertTrue(enHtml.contains("Welcome to GoodOne"));
        
        // German
        String deHtml = (String) ReflectionTestUtils.invokeMethod(emailService, "getEmailHtml", baseUrl, true, true);
        saveEmailToFile("verification-de-ch.html", deHtml);
        assertTrue(deHtml.contains("Willkommen bei GoodOne"));
    }

    private void saveEmailToFile(String filename, String content) throws IOException {
        // Use a relative path from project root if possible, or target of backend
        Path path = Paths.get("target", "generated-emails", filename);
        // If running from project root, target might be in backend/target
        if (!Files.exists(Paths.get("backend")) && Files.exists(Paths.get("src"))) {
             // likely already in backend
        } else if (Files.exists(Paths.get("backend"))) {
             path = Paths.get("backend", "target", "generated-emails", filename);
        }
        
        Files.createDirectories(path.getParent());
        Files.writeString(path, content);
        System.out.println("Email saved to: " + path.toAbsolutePath());
    }
}
