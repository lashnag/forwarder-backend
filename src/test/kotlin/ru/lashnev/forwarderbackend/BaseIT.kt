package ru.lashnev.forwarderbackend

import com.pengrad.telegrambot.model.User
import com.pengrad.telegrambot.request.SendMessage
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import ru.lashnev.forwarderbackend.dao.GroupsDao
import ru.lashnev.forwarderbackend.dao.SubscribersDao
import ru.lashnev.forwarderbackend.dao.SubscriptionDao

@SpringBootTest
@ActiveProfiles("test")
class BaseIT {

    @Autowired
    protected lateinit var subscriptionDao: SubscriptionDao

    @Autowired
    protected lateinit var subscribersDao: SubscribersDao

    @Autowired
    protected lateinit var groupsDao: GroupsDao

    protected val user = mock(User::class.java)
    protected val captor: ArgumentCaptor<SendMessage> = ArgumentCaptor.forClass(SendMessage::class.java)

    @BeforeEach
    fun setUp() {
        `when`(user.id()).thenReturn(1)
        `when`(user.username()).thenReturn(testUsername)
    }

    @AfterEach
    fun clearDb() {
        subscriptionDao.deleteAll()
    }

    companion object {
        @JvmStatic
        protected val testUsername = "lashnag"

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