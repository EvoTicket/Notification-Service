package capstone.notificationservice.config;

public record RedisConfigData(String host, int port, String username, String password, boolean useSsl) {}

