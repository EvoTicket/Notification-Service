package capstone.notificationservice.service;

import capstone.notificationservice.exception.AppException;
import capstone.notificationservice.exception.ErrorCode;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import sendinblue.ApiException;
import sibApi.TransactionalEmailsApi;
import sibModel.SendSmtpEmail;
import sibModel.SendSmtpEmailSender;
import sibModel.SendSmtpEmailTo;

import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final TransactionalEmailsApi emailsApi;
//    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${brevo.sender-email}")
    private String senderEmail;

    @Value("${brevo.sender-name}")
    private String senderName;

//    public void sendOtpEmail(String toEmail, String otpCode) {
//        try {
//            MimeMessage message = mailSender.createMimeMessage();
//            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
//
//            helper.setTo(toEmail);
//            helper.setSubject("Mã OTP xác thực - EvoTicket");
//            helper.setFrom("evoticket.work@gmail.com", "EvoTicket");
//
//            Context context = new Context();
//            context.setVariable("otpCode", otpCode);
//            context.setVariable("email", toEmail);
//
//            String htmlContent = templateEngine.process("otp-email", context);
//            helper.setText(htmlContent, true);
//
//            mailSender.send(message);
//            log.info("OTP email sent successfully to: {}", toEmail);
//
//        } catch (MessagingException e) {
//            throw new AppException(ErrorCode.MESSAGE_ERROR);
//        } catch (Exception e) {
//            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR, "Unexpected error while sending email", e);
//        }
//    }
//
//    public void sendWelcomeEmail(String toEmail, String fullName, String username) {
//        try {
//            MimeMessage message = mailSender.createMimeMessage();
//            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
//
//            helper.setTo(toEmail);
//            helper.setSubject("Chào mừng đến với EvoTicket! 🎉");
//            helper.setFrom("evoticket.work@gmail.com", "EvoTicket");
//
//            Context context = new Context();
//            context.setVariable("email", toEmail);
//            context.setVariable("fullName", fullName);
//            context.setVariable("username", username);
//
//            String htmlContent = templateEngine.process("welcome-email", context);
//            helper.setText(htmlContent, true);
//
//            mailSender.send(message);
//            log.info("Welcome email sent successfully to: {}", toEmail);
//
//        } catch (MessagingException e) {
//            throw new AppException(ErrorCode.MESSAGE_ERROR);
//        } catch (Exception e) {
//            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR, "Unexpected error while sending email", e);
//        }
//    }

    public void sendWelcomeEmail(String toEmail, String fullName, String username) {
        try {
            Context context = new Context();
            context.setVariable("email", toEmail);
            context.setVariable("fullName", fullName);
            context.setVariable("username", username);

            String htmlContent = templateEngine.process("welcome-email", context);

            SendSmtpEmail email = new SendSmtpEmail();

            SendSmtpEmailSender sender = new SendSmtpEmailSender();
            sender.setEmail(senderEmail);
            sender.setName(senderName);
            email.setSender(sender);

            SendSmtpEmailTo recipient = new SendSmtpEmailTo();
            recipient.setEmail(toEmail);
            recipient.setName(fullName);
            List<SendSmtpEmailTo> toList = Collections.singletonList(recipient);
            email.setTo(toList);

            email.setSubject("Chào mừng đến với EvoTicket! 🎉");
            email.setHtmlContent(htmlContent);

            emailsApi.sendTransacEmail(email);

            log.info("Welcome email sent successfully to: {}", toEmail);

        } catch (ApiException e) {
            log.error("Brevo API error while sending email to {}: {}", toEmail, e.getMessage());
            throw new AppException(ErrorCode.MESSAGE_ERROR, "Failed to send email via Brevo", e);
        } catch (Exception e) {
            log.error("Unexpected error while sending email to {}: {}", toEmail, e.getMessage());
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR, "Unexpected error while sending email", e);
        }
    }
}
