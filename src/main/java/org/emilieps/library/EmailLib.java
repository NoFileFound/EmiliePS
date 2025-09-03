package org.emilieps.library;

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
import org.emilieps.Application;

public final class EmailLib {
    private static Session session;

    /**
     * Inits the smtp config.
     */
    public static void initSmtpConfig() {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", Application.getHttpConfig().email_info.smtpHost);
        props.put("mail.smtp.port", Application.getHttpConfig().email_info.smtpPort);

        session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(Application.getHttpConfig().email_info.smtpUsername, Application.getHttpConfig().email_info.smtpPassword);
            }
        });
    }

    /**
     * Sends an email message.
     *
     * @param toEmail     The sender.
     * @param subject     The email subject. (title).
     * @param messageBody The email content. (message)
     */
    public static void sendMessage(String toEmail, String subject, String messageBody) {
        if (session == null) {
            return;
        }

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(Application.getHttpConfig().email_info.smtpUsername));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject(subject);
            message.setText(messageBody);

            Transport.send(message);

        } catch (MessagingException ignored) {
        }
    }
}