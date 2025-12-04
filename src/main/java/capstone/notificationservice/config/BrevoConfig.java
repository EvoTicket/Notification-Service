package capstone.notificationservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import sendinblue.ApiClient;
import sibApi.TransactionalEmailsApi;

@Configuration
public class BrevoConfig {

    @Value("${brevo.api-key}")
    private String apiKey;

    @Bean
    public TransactionalEmailsApi transactionalEmailsApi() {
        ApiClient defaultClient = sendinblue.Configuration.getDefaultApiClient();
        defaultClient.setApiKey(apiKey);
        return new TransactionalEmailsApi();
    }
}