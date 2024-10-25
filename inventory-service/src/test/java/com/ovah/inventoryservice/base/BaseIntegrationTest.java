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
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import org.springframework.jdbc.core.JdbcTemplate;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Duration;
import java.util.List;

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
    }

    @AfterAll
    static void afterAll() {
        postgres.stop();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @BeforeEach
    void setUp() {
        truncateAllTables();
    }

    @AfterEach
    void tearDown() {
        truncateAllTables();
    }

    protected void truncateAllTables() {
        transactionTemplate.execute(status -> {
            try {
                entityManager.createNativeQuery("SET CONSTRAINTS ALL DEFERRED").executeUpdate();

                List<String> tableNames = jdbcTemplate.queryForList(
                        "SELECT tablename FROM pg_tables WHERE schemaname = 'public'",
                        String.class
                );

                for (String tableName : tableNames) {
                    if (!tableName.equals("flyway_schema_history")) {
                        jdbcTemplate.execute("TRUNCATE TABLE " + tableName + " CASCADE");
                    }
                }

                entityManager.createNativeQuery("SET CONSTRAINTS ALL IMMEDIATE").executeUpdate();
                return null;
            } catch (Exception e) {
                throw new RuntimeException("Failed to truncate tables", e);
            }
        });
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
}
