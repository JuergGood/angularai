package ch.goodone.angularai.backend.service;

import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

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
        sendVerificationEmail(toEmail, token, LocaleContextHolder.getLocale());
    }

    public void sendVerificationEmail(String toEmail, String token, Locale locale) {
        String verificationUrl = baseUrl + "/verify?token=" + token;
        boolean isGerman = locale != null && "de".equals(locale.getLanguage());

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, StandardCharsets.UTF_8.name());

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            
            String subject = isGerman ? "Verifizieren Sie Ihr GoodOne-Konto" : "Verify your GoodOne account";
            helper.setSubject(subject);

            String htmlContent = getEmailHtml(verificationUrl, isGerman, true);
            
            String textContent;
            if (isGerman) {
                textContent = "Vielen Dank für Ihre Registrierung. Bitte klicken Sie auf den untenstehenden Link, um Ihr Konto zu verifizieren:\n\n" + verificationUrl;
            } else {
                textContent = "Thank you for registering. Please click the link below to verify your account:\n\n" + verificationUrl;
            }

            helper.setText(textContent, htmlContent);

            mailSender.send(message);
            logger.info("Verification email sent successfully");
        } catch (Exception e) {
            logger.error("Failed to send verification email");
        }
    }

    public void sendPasswordRecoveryEmail(String toEmail, String token, Locale locale) {
        String recoveryUrl = baseUrl + "/reset-password?token=" + token;
        boolean isGerman = locale != null && "de".equals(locale.getLanguage());

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, StandardCharsets.UTF_8.name());

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            
            String subject = isGerman ? "Passwort-Wiederherstellung - GoodOne" : "Password Recovery - GoodOne";
            helper.setSubject(subject);

            String htmlContent = getEmailHtml(recoveryUrl, isGerman, false);
            
            String textContent;
            if (isGerman) {
                textContent = "Sie haben eine Passwort-Wiederherstellung angefordert. Bitte klicken Sie auf den untenstehenden Link, um Ihr Passwort zurückzusetzen:\n\n" + recoveryUrl;
            } else {
                textContent = "You have requested a password recovery. Please click the link below to reset your password:\n\n" + recoveryUrl;
            }

            helper.setText(textContent, htmlContent);

            mailSender.send(message);
            logger.info("Password recovery email sent successfully");
        } catch (Exception e) {
            logger.error("Failed to send password recovery email");
        }
    }

    private String getEmailHtml(String url, boolean isGerman, boolean isVerification) {
        HtmlEmailContext ctx = new HtmlEmailContext();
        ctx.setUrl(url);
        ctx.setGerman(isGerman);
        ctx.setVerification(isVerification);
        ctx.setTitle(resolveTitle(isGerman, isVerification));
        ctx.setWelcome(isGerman ? "Willkommen bei GoodOne" : "Welcome to GoodOne");
        ctx.setThanks(resolveThanks(isGerman, isVerification));
        ctx.setInstruction(resolveInstruction(isGerman, isVerification));
        ctx.setButtonText(resolveButtonText(isGerman, isVerification));
        ctx.setFallbackText(isGerman ? "Wenn die Schaltfläche nicht funktioniert, kopieren Sie diesen Link und fügen Sie ihn in Ihren Browser ein:" : "If the button doesn’t work, copy and paste this link into your browser:");
        ctx.setIgnoreText(isGerman ? "Wenn Sie diese E-Mail nicht angefordert haben, können Sie sie ignorieren." : "If you did not request this email, you can safely ignore it.");
        ctx.setBestRegards(isGerman ? "Freundliche Grüsse," : "Best regards,");
        ctx.setTeam(isGerman ? "Das GoodOne Team" : "The GoodOne Team");

        return buildHtml(ctx);
    }

    private static class HtmlEmailContext {
        private String url;
        private boolean isGerman;
        private boolean isVerification;
        private String title;
        private String welcome;
        private String thanks;
        private String instruction;
        private String buttonText;
        private String fallbackText;
        private String ignoreText;
        private String bestRegards;
        private String team;

        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }
        public boolean isGerman() { return isGerman; }
        public void setGerman(boolean german) { isGerman = german; }
        public boolean isVerification() { return isVerification; }
        public void setVerification(boolean verification) { isVerification = verification; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getWelcome() { return welcome; }
        public void setWelcome(String welcome) { this.welcome = welcome; }
        public String getThanks() { return thanks; }
        public void setThanks(String thanks) { this.thanks = thanks; }
        public String getInstruction() { return instruction; }
        public void setInstruction(String instruction) { this.instruction = instruction; }
        public String getButtonText() { return buttonText; }
        public void setButtonText(String buttonText) { this.buttonText = buttonText; }
        public String getFallbackText() { return fallbackText; }
        public void setFallbackText(String fallbackText) { this.fallbackText = fallbackText; }
        public String getIgnoreText() { return ignoreText; }
        public void setIgnoreText(String ignoreText) { this.ignoreText = ignoreText; }
        public String getBestRegards() { return bestRegards; }
        public void setBestRegards(String bestRegards) { this.bestRegards = bestRegards; }
        public String getTeam() { return team; }
        public void setTeam(String team) { this.team = team; }
    }

    private String resolveTitle(boolean isGerman, boolean isVerification) {
        if (isVerification) {
            return isGerman ? "E-Mail-Adresse bestätigen" : "Confirm your email";
        }
        return isGerman ? "Passwort zurücksetzen" : "Reset your password";
    }

    private String resolveThanks(boolean isGerman, boolean isVerification) {
        if (isVerification) {
            return isGerman ? "Vielen Dank für die Erstellung eines GoodOne-Kontos." : "Thanks for creating a GoodOne account.";
        }
        return isGerman ? "Sie haben eine Passwort-Wiederherstellung für Ihr GoodOne-Konto angefordert." : "You requested a password recovery for your GoodOne account.";
    }

    private String resolveInstruction(boolean isGerman, boolean isVerification) {
        if (isVerification) {
            return isGerman ? "Um Ihre Registrierung abzuschliessen, bestätigen Sie bitte Ihre E-Mail-Adresse, indem Sie auf die Schaltfläche unten klicken:" : "To complete your registration, please confirm your email address by clicking the button below:";
        }
        return isGerman ? "Um Ihr Passwort zurückzusetzen, klicken Sie bitte auf die Schaltfläche unten:" : "To reset your password, please click the button below:";
    }

    private String resolveButtonText(boolean isGerman, boolean isVerification) {
        if (isVerification) {
            return isGerman ? "E-Mail-Adresse bestätigen" : "Confirm email address";
        }
        return isGerman ? "Passwort zurücksetzen" : "Reset password";
    }

    private String buildHtml(HtmlEmailContext ctx) {
        String welcomeText = ctx.isVerification() ? ctx.getWelcome() : ctx.getTitle();
        return String.format("<!DOCTYPE html>\n" +
                "<html lang=\"%s\">\n" +
                "  <head>\n" +
                "    <meta charset=\"UTF-8\" />\n" +
                "    <title>%s</title>\n" +
                "  </head>\n" +
                "  <body style=\"margin:0; padding:0; background-color:#f5f7fa; font-family:Arial, Helvetica, sans-serif;\">\n" +
                "    <table width=\"100%%\" cellpadding=\"0\" cellspacing=\"0\">\n" +
                "      <tr>\n" +
                "        <td align=\"center\" style=\"padding:40px 16px;\">\n" +
                "          <table width=\"100%%\" cellpadding=\"0\" cellspacing=\"0\" style=\"max-width:520px; background:#ffffff; border-radius:8px; box-shadow:0 2px 8px rgba(0,0,0,0.08);\">\n" +
                "\n" +
                "            <!-- Header -->\n" +
                "            <tr>\n" +
                "              <td style=\"padding:24px 24px 16px; text-align:center;\">\n" +
                "                <h1 style=\"margin:0; font-size:22px; color:#1f2937;\">\n" +
                "                  %s\n" +
                "                </h1>\n" +
                "              </td>\n" +
                "            </tr>\n" +
                "\n" +
                "            <!-- Content -->\n" +
                "            <tr>\n" +
                "              <td style=\"padding:0 24px 24px; color:#374151; font-size:14px; line-height:1.6;\">\n" +
                "                <p>\n" +
                "                  %s\n" +
                "                </p>\n" +
                "\n" +
                "                <p>\n" +
                "                  %s\n" +
                "                </p>\n" +
                "\n" +
                "                <!-- Button -->\n" +
                "                <p style=\"text-align:center; margin:32px 0;\">\n" +
                "                  <a\n" +
                "                    href=\"%s\"\n" +
                "                    style=\"\n" +
                "                      display:inline-block;\n" +
                "                      padding:12px 24px;\n" +
                "                      background:#3b82f6;\n" +
                "                      color:#ffffff;\n" +
                "                      text-decoration:none;\n" +
                "                      border-radius:6px;\n" +
                "                      font-weight:bold;\n" +
                "                    \"\n" +
                "                  >\n" +
                "                    %s\n" +
                "                  </a>\n" +
                "                </p>\n" +
                "\n" +
                "                <p style=\"font-size:13px; color:#6b7280;\">\n" +
                "                  %s\n" +
                "                </p>\n" +
                "\n" +
                "                <p style=\"font-size:12px; word-break:break-all; color:#2563eb;\">\n" +
                "                  %s\n" +
                "                </p>\n" +
                "\n" +
                "                <p style=\"font-size:13px; color:#6b7280; margin-top:24px;\">\n" +
                "                  %s\n" +
                "                </p>\n" +
                "\n" +
                "                <p style=\"margin-top:32px;\">\n" +
                "                  %s<br />\n" +
                "                  <strong>%s</strong>\n" +
                "                </p>\n" +
                "              </td>\n" +
                "            </tr>\n" +
                "\n" +
                "          </table>\n" +
                "        </td>\n" +
                "      </tr>\n" +
                "    </table>\n" +
                "  </body>\n" +
                "</html>",
                ctx.isGerman() ? "de" : "en",
                ctx.getTitle(),
                welcomeText,
                ctx.getThanks(),
                ctx.getInstruction(),
                ctx.getUrl(),
                ctx.getButtonText(),
                ctx.getFallbackText(),
                ctx.getUrl(),
                ctx.getIgnoreText(),
                ctx.getBestRegards(),
                ctx.getTeam());
    }
}
