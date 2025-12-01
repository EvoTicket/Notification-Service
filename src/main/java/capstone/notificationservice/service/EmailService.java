package capstone.notificationservice.service;

import capstone.notificationservice.exception.AppException;
import capstone.notificationservice.exception.ErrorCode;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    public void sendOtpEmail(String toEmail, String otpCode) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject("Mã OTP xác thực - EvoTicket");
            helper.setFrom("evoticket.work@gmail.com", "EvoTicket");

            Context context = new Context();
            context.setVariable("otpCode", otpCode);
            context.setVariable("email", toEmail);

            String htmlContent = templateEngine.process("otp-email", context);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("OTP email sent successfully to: {}", toEmail);

        } catch (MessagingException e) {
            throw new AppException(ErrorCode.MESSAGE_ERROR);
        } catch (Exception e) {
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR, "Unexpected error while sending email", e);
        }
    }

    public void sendWelcomeEmail(String toEmail, String fullName, String username) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject("Chào mừng đến với EvoTicket! 🎉");
            helper.setFrom("evoticket.work@gmail.com", "EvoTicket");

            Context context = new Context();
            context.setVariable("email", toEmail);
            context.setVariable("fullName", fullName);
            context.setVariable("username", username);

            String htmlContent = templateEngine.process("welcome-email", context);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Welcome email sent successfully to: {}", toEmail);

        } catch (MessagingException e) {
            throw new AppException(ErrorCode.MESSAGE_ERROR);
        } catch (Exception e) {
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR, "Unexpected error while sending email", e);
        }
    }
}
