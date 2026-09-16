package com.example.url_shortener;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
@TestPropertySource(properties = {
        "JWT_SECRET=this-is-a-test-secret-key-with-enough-length-123456",
        "JWT_EXPIRATION=86400000"
})
class AppLauncherTests {
    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:18-alpine")
                    .withDatabaseName("url_shortener")
                    .withUsername("postgres")
                    .withPassword("postgres");

    @Test
    void contextLoads() {
    }
}