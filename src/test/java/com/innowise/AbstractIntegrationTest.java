package com.innowise;

import com.redis.testcontainers.RedisContainer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Testcontainers
@ActiveProfiles("test")
public abstract class AbstractIntegrationTest {

    @ServiceConnection
    private static final PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:16-alpine")
            .waitingFor(Wait.forListeningPort());

    @ServiceConnection
    private static final RedisContainer redisContainer = new RedisContainer(DockerImageName.parse("redis:6.2.2"))
            .waitingFor(Wait.forListeningPort());
}