package org.genshinimpact.webserver.utils;

// Imports
import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.util.Properties;
import org.genshinimpact.bootstrap.AppBootstrap;
import org.genshinimpact.webserver.SpringBootApp;

public final class SMTPUtils {
    private static Session session;

    /**
     * Inits the smtp config.
     */
    public static void initSmtpConfig() {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", SpringBootApp.getWebConfig().SMTP.smtpHost);
        props.put("mail.smtp.port", SpringBootApp.getWebConfig().SMTP.smtpPort);

        session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SpringBootApp.getWebConfig().SMTP.smtpUsername, SpringBootApp.getWebConfig().SMTP.smtpPassword);
            }
        });

        try(Transport transport = session.getTransport("smtp")) {
            transport.connect();
            AppBootstrap.getLogger().info("SMTP was loaded successfully.");
        } catch(MessagingException e) {
            AppBootstrap.getLogger().error("Failed to connect to SMTP server.", e);
        }
    }

    /**
     * Sends device verification email notification.
     * @param emailAddress The provided email address.
     * @param verCode The provided verification code.
     */
    public static void sendDeviceVerificationEmailMessage(String emailAddress, String verCode) {
        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(SpringBootApp.getWebConfig().SMTP.smtpUsername, "EmiliePS Team"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(emailAddress));
            message.setSubject("Device Verification | Genshin Impact");
            String html = "<html>" +
                    "<body style='font-family: Arial, sans-serif; color: #333; background-color: #f5f5f5;'>" +
                    "<div style='max-width: 600px; margin: auto; padding: 20px; background: #ffffff; border-radius: 10px;'>" +
                    "<h2 style='color: #1E90FF;'>Genshin Impact Device Verification</h2>" +
                    "<p>Hello Traveler,</p>" +
                    "<p>We received a request to verify your device for your Genshin Impact account.</p>" +
                    "<p>Your verification code is:</p>" +
                    "<div style='font-size: 24px; font-weight: bold; margin: 20px 0; color: #FF4500;'>" +
                    verCode +
                    "</div>" +
                    "<p>Please enter this code in the app to verify your device.</p>" +
                    "<p>If you did not request this, please ignore this email.</p>" +
                    "<hr>" +
                    "<p style='font-size: 12px; color: #888;'>This is an automated message from miHoYo. Do not reply.</p>" +
                    "</div>" +
                    "</body>" +
                    "</html>";

            message.setContent(html, "text/html; charset=utf-8");
            Transport.send(message);
            AppBootstrap.getLogger().info("[Email] Device verification email sent to {}", emailAddress);
        } catch(Exception e) {
            AppBootstrap.getLogger().error("[Email] Failed to send device verification email to {}", emailAddress, e);
        }
    }
}