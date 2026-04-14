package capstone.notificationservice.service;

import capstone.notificationservice.exception.AppException;
import capstone.notificationservice.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Slf4j
@Service
@RequiredArgsConstructor
public class SmtpEmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:your-email@gmail.com}")
    private String fromEmail;

    @Value("${spring.mail.from-name:EvoTicket}")
    private String fromName;

    /**
     * Gửi email đơn giản (plain text)
     */
    public void sendSimpleEmail(String toEmail, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject(subject);
            message.setText(body);

            mailSender.send(message);
            log.info("Simple email sent successfully to: {}", toEmail);

        } catch (Exception e) {
            log.error("Error sending simple email to {}: {}", toEmail, e.getMessage());
            throw new AppException(ErrorCode.MESSAGE_ERROR, "Failed to send simple email", e);
        }
    }

    /**
     * Gửi email HTML
     */
    public void sendHtmlEmail(String toEmail, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, fromName);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlContent, true); // true for HTML

            mailSender.send(message);
            log.info("HTML email sent successfully to: {}", toEmail);

        } catch (MessagingException e) {
            log.error("MessagingException while sending HTML email to {}: {}", toEmail, e.getMessage());
            throw new AppException(ErrorCode.MESSAGE_ERROR, "Failed to send HTML email", e);
        } catch (Exception e) {
            log.error("Unexpected error while sending HTML email to {}: {}", toEmail, e.getMessage());
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR, "Unexpected error while sending HTML email", e);
        }
    }

    /**
     * Gửi email HTML với multiple recipients
     */
    public void sendHtmlEmailToMultiple(String[] toEmails, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, fromName);
            helper.setTo(toEmails);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("HTML email sent successfully to: {} recipients", toEmails.length);

        } catch (MessagingException e) {
            log.error("MessagingException while sending HTML email to multiple recipients: {}", e.getMessage());
            throw new AppException(ErrorCode.MESSAGE_ERROR, "Failed to send HTML email to multiple recipients", e);
        } catch (Exception e) {
            log.error("Unexpected error while sending HTML email to multiple recipients: {}", e.getMessage());
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR, "Unexpected error while sending HTML email", e);
        }
    }

    /**
     * Gửi email HTML với CC và BCC
     */
    public void sendHtmlEmailWithCcBcc(String toEmail, String[] ccEmails, String[] bccEmails,
                                       String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, fromName);
            helper.setTo(toEmail);

            if (ccEmails != null && ccEmails.length > 0) {
                helper.setCc(ccEmails);
            }

            if (bccEmails != null && bccEmails.length > 0) {
                helper.setBcc(bccEmails);
            }

            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("HTML email with CC/BCC sent successfully to: {}", toEmail);

        } catch (MessagingException e) {
            log.error("MessagingException while sending HTML email with CC/BCC to {}: {}", toEmail, e.getMessage());
            throw new AppException(ErrorCode.MESSAGE_ERROR, "Failed to send HTML email with CC/BCC", e);
        } catch (Exception e) {
            log.error("Unexpected error while sending HTML email with CC/BCC to {}: {}", toEmail, e.getMessage());
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR, "Unexpected error while sending HTML email", e);
        }
    }
}
