package ru.lashnev.forwarderbackend

import org.junit.jupiter.api.AfterEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import ru.lashnev.forwarderbackend.dao.SubscriptionDao

@SpringBootTest
@ActiveProfiles("test")
class BaseIT {

    @Autowired
    protected lateinit var subscriptionDao: SubscriptionDao

    @AfterEach
    fun clearDb() {
        subscriptionDao.deleteAll()
    }

    companion object {
        private val postgreSQLContainer = PostgreSQLContainer("postgres:latest")
            .withDatabaseName("telegram_forwarder")
            .withUsername("test")
            .withPassword("test")

        init {
            postgreSQLContainer.start() // Запускаем контейнер
        }

        @JvmStatic
        @DynamicPropertySource
        fun properties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url") { postgreSQLContainer.jdbcUrl }
            registry.add("spring.datasource.username") { postgreSQLContainer.username }
            registry.add("spring.datasource.password") { postgreSQLContainer.password }
        }
    }
}