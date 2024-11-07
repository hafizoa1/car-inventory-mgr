package com.ovah.inventoryservice.base;


import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import org.springframework.jdbc.core.JdbcTemplate;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.IOException;
import java.time.Duration;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers
public abstract class BaseIntegrationTest {

    @LocalServerPort
    protected int port;

    @Autowired
    protected TestRestTemplate restTemplate;

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    @Autowired
    protected TransactionTemplate transactionTemplate;

    @PersistenceContext
    protected EntityManager entityManager;

    @Container
    static RabbitMQContainer rabbitMQ = new RabbitMQContainer(
            DockerImageName.parse("rabbitmq:3-management")
    )
            .withExposedPorts(5672, 15672)
            .withStartupTimeout(Duration.ofMinutes(2));

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            DockerImageName.parse("postgres:14")
    )
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test")
            .withStartupTimeout(Duration.ofMinutes(2));

    @BeforeAll
    static void beforeAll() {
        postgres.start();
        rabbitMQ.start();
    }

    @AfterAll
    static void afterAll() {
        postgres.stop();
        rabbitMQ.stop();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);

        registry.add("spring.rabbitmq.host", rabbitMQ::getHost);
        registry.add("spring.rabbitmq.port", rabbitMQ::getAmqpPort);
        registry.add("spring.rabbitmq.username", rabbitMQ::getAdminUsername);
        registry.add("spring.rabbitmq.password", rabbitMQ::getAdminPassword);
    }

    @BeforeEach
    void setUp() throws Exception {
        resetRabbitMQ();
    }

    @AfterEach
    void tearDown() throws Exception {
        resetRabbitMQ();
    }

    private void resetRabbitMQ() {
        try {
            rabbitMQ.execInContainer(
                    "rabbitmqctl", "stop_app",
                    "rabbitmqctl", "reset",
                    "rabbitmqctl", "start_app"
            );
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Failed to reset RabbitMQ", e);
        }
    }

    // Utility methods
    protected String createURLWithPort(String uri) {
        return "http://localhost:" + port + uri;
    }

    protected <T> T persistAndFlush(T entity) {
        return transactionTemplate.execute(status -> {
            entityManager.persist(entity);
            entityManager.flush();
            return entity;
        });
    }

    protected void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }

    protected <T> T refreshEntity(T entity) {
        entityManager.flush();
        entityManager.clear();
        return entityManager.find((Class<T>) entity.getClass(),
                entityManager.getEntityManagerFactory()
                        .getPersistenceUnitUtil()
                        .getIdentifier(entity));
    }

    protected <T> T doInTransaction(TransactionCallback<T> callback) {
        return transactionTemplate.execute(status -> {
            try {
                return callback.execute();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    @FunctionalInterface
    protected interface TransactionCallback<T> {
        T execute() throws Exception;
    }
}