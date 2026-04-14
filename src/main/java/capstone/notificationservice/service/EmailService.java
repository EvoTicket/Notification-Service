package capstone.notificationservice.service;

import capstone.notificationservice.event.OrderConfirmEvent;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final TemplateEngine templateEngine;
    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.from-name:}")
    private String fromName;

    public void sendHtmlEmail(String toEmail, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, fromName);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

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

    public void sendOtpEmail(String toEmail, String otpCode) {
        Context context = new Context();
        context.setVariable("otpCode", otpCode);
        context.setVariable("email", toEmail);

        String htmlContent = templateEngine.process("otp-email", context);
        String subject = "Mã OTP xác thực - EvoTicket";
        sendHtmlEmail(toEmail, subject, htmlContent);
    }

    public void sendWelcomeEmail(String toEmail, String fullName, String username) {
        Context context = new Context();
        context.setVariable("email", toEmail);
        context.setVariable("fullName", fullName);
        context.setVariable("username", username);

        String htmlContent = templateEngine.process("welcome-email", context);
        String subject = "Chào mừng đến với EvoTicket! 🎉";

        sendHtmlEmail(toEmail, subject, htmlContent);
    }

    public void sendOrderConfirmEmail(OrderConfirmEvent dto) {
        Context context = new Context();
        context.setVariable("email", dto.getEmail());
        context.setVariable("fullName", dto.getFullName());

        // Order info
        context.setVariable("orderCode", dto.getOrderCode());
        context.setVariable("totalAmount", dto.getTotalAmount());
        context.setVariable("discountCode", dto.getDiscountCode());
        context.setVariable("discountAmount", dto.getDiscountAmount());
        context.setVariable("ticketDownloadUrl", dto.getTicketDownloadUrl());

        // Event info
        context.setVariable("eventName", dto.getEventName());
        context.setVariable("eventDate", dto.getEventDate());
        context.setVariable("eventTime", dto.getEventTime());
        context.setVariable("eventLocation", dto.getEventLocation());
        context.setVariable("eventAddress", dto.getEventAddress());
        context.setVariable("organizerName", dto.getOrganizerName());

        // Showtime info
        context.setVariable("showtimeDate", dto.getShowtimeDate());
        context.setVariable("showtimeTime", dto.getShowtimeTime());
        context.setVariable("showtimeLocation", dto.getShowtimeLocation());
        context.setVariable("showtimeAddress", dto.getShowtimeAddress());

        // Payment info
        context.setVariable("paymentMethod", dto.getPaymentMethod());
        context.setVariable("transactionId", dto.getTransactionId());
        context.setVariable("paidAt", dto.getPaidAt());

        // Ticket items
        context.setVariable("ticketItems", dto.getTicketItems());

        String toEmail = dto.getEmail();
        String htmlContent = templateEngine.process("order-confirm-email", context);
        String subject = "Thanh toán thành công - " + dto.getEventName() + " 🎫";

        sendHtmlEmail(toEmail, subject, htmlContent);
    }
}
