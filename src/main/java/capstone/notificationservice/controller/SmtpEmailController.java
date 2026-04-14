package capstone.notificationservice.controller;

import capstone.notificationservice.dto.SendEmailRequest;
import capstone.notificationservice.service.SmtpEmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/email")
@RequiredArgsConstructor
public class SmtpEmailController {

    private final SmtpEmailService smtpEmailService;

    /**
     * API gửi email đơn giản (plain text hoặc HTML)
     * POST /api/v1/email/send
     *
     * @param request SendEmailRequest chứa:
     *                - toEmail: địa chỉ email nhận
     *                - subject: chủ đề email
     *                - body: nội dung email
     *                - isHtml: true nếu gửi HTML, false nếu gửi plain text
     *                - ccEmails (optional): danh sách email CC
     *                - bccEmails (optional): danh sách email BCC
     * @return ResponseEntity với status và message
     */
    @PostMapping("/send")
    public ResponseEntity<Map<String, Object>> sendEmail(@Valid @RequestBody SendEmailRequest request) {
        try {
            log.info("Received email request to: {}", request.getToEmail());

            // Gửi email dựa trên loại
            if (request.getIsHtml() != null && request.getIsHtml()) {
                // Nếu có CC/BCC
                if ((request.getCcEmails() != null && request.getCcEmails().length > 0) ||
                    (request.getBccEmails() != null && request.getBccEmails().length > 0)) {
                    smtpEmailService.sendHtmlEmailWithCcBcc(
                        request.getToEmail(),
                        request.getCcEmails(),
                        request.getBccEmails(),
                        request.getSubject(),
                        request.getBody()
                    );
                } else {
                    // Gửi HTML email đơn giản
                    smtpEmailService.sendHtmlEmail(
                        request.getToEmail(),
                        request.getSubject(),
                        request.getBody()
                    );
                }
            } else {
                // Gửi plain text email
                smtpEmailService.sendSimpleEmail(
                    request.getToEmail(),
                    request.getSubject(),
                    request.getBody()
                );
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Email sent successfully");
            response.put("toEmail", request.getToEmail());
            response.put("subject", request.getSubject());

            log.info("Email sent successfully to: {}", request.getToEmail());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error sending email: {}", e.getMessage());

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to send email: " + e.getMessage());
            response.put("error", e.getClass().getSimpleName());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * API gửi email đơn giản (plain text)
     * POST /api/v1/email/send-simple
     */
    @PostMapping("/send-simple")
    public ResponseEntity<Map<String, Object>> sendSimpleEmail(
            @RequestParam String toEmail,
            @RequestParam String subject,
            @RequestParam String body) {
        try {
            log.info("Sending simple email to: {}", toEmail);
            smtpEmailService.sendSimpleEmail(toEmail, subject, body);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Simple email sent successfully");
            response.put("toEmail", toEmail);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error sending simple email: {}", e.getMessage());

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to send simple email: " + e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * API gửi email HTML
     * POST /api/v1/email/send-html
     */
    @PostMapping("/send-html")
    public ResponseEntity<Map<String, Object>> sendHtmlEmail(
            @RequestParam String toEmail,
            @RequestParam String subject,
            @RequestParam String htmlContent) {
        try {
            log.info("Sending HTML email to: {}", toEmail);
            smtpEmailService.sendHtmlEmail(toEmail, subject, htmlContent);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "HTML email sent successfully");
            response.put("toEmail", toEmail);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error sending HTML email: {}", e.getMessage());

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to send HTML email: " + e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * API test - trả về thông tin cấu hình email
     * GET /api/v1/email/test
     */
    @GetMapping("/test")
    public ResponseEntity<Map<String, String>> testEmail() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "ok");
        response.put("message", "Email service is running");
        response.put("endpoints", "POST /api/v1/email/send, POST /api/v1/email/send-simple, POST /api/v1/email/send-html");

        return ResponseEntity.ok(response);
    }
}
