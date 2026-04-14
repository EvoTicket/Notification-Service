package capstone.notificationservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendEmailRequest {

    @Email(message = "Email phải là địa chỉ email hợp lệ")
    @NotBlank(message = "Email không được để trống")
    private String toEmail;

    @NotBlank(message = "Chủ đề email không được để trống")
    private String subject;

    @NotBlank(message = "Nội dung email không được để trống")
    private String body;

    @JsonProperty("isHtml")
    private Boolean isHtml = false;

    @JsonProperty("ccEmails")
    private String[] ccEmails;

    @JsonProperty("bccEmails")
    private String[] bccEmails;
}
