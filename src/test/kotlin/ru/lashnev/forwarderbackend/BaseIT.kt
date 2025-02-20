package ru.lashnev.forwarderbackend

import com.github.lashnag.telegrambotstarter.UpdatesScheduler
import com.pengrad.telegrambot.TelegramBot
import com.pengrad.telegrambot.model.User
import com.pengrad.telegrambot.request.SendMessage
import com.pengrad.telegrambot.response.SendResponse
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.mockito.ArgumentCaptor
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName
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

    @MockBean
    protected lateinit var telegramBot: TelegramBot

    @MockBean
    protected lateinit var updatesScheduler: UpdatesScheduler

    protected val user = mock<User>()
    protected val captor: ArgumentCaptor<SendMessage> = ArgumentCaptor.forClass(SendMessage::class.java)

    @BeforeEach
    fun setUp() {
        whenever(user.id()).thenReturn(1)
        whenever(user.username()).thenReturn(testUsername)
        val messageResponse = mock<SendResponse>()
        whenever(messageResponse.errorCode()).thenReturn(0)
        whenever(telegramBot.execute(any<SendMessage>())).thenReturn(messageResponse)
    }

    @AfterEach
    fun clearDb() {
        subscriptionDao.deleteAll()
    }

    companion object {
        @JvmStatic
        protected val testUsername = "lashnag"

        private val postgreSQLContainer =
            PostgreSQLContainer("postgres:latest")
                .withDatabaseName("telegram_forwarder")
                .withUsername("test")
                .withPassword("test")

        private val lemmatizerContainer = GenericContainer(DockerImageName.parse("lashnag/lemmatizer")).withExposedPorts(4355)
        private val ocrContainer = GenericContainer(DockerImageName.parse("lashnag/ocr")).withExposedPorts(4366)

        init {
            postgreSQLContainer.start()
            lemmatizerContainer.start()
            ocrContainer.start()
        }

        @JvmStatic
        @DynamicPropertySource
        fun properties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url") { postgreSQLContainer.jdbcUrl }
            registry.add("spring.datasource.username") { postgreSQLContainer.username }
            registry.add("spring.datasource.password") { postgreSQLContainer.password }

            val lemmatizerPort = lemmatizerContainer.firstMappedPort
            registry.add("api.lemmatization-url") { "http://127.0.0.1:$lemmatizerPort/lemmatize" }

            val ocrPort = ocrContainer.firstMappedPort
            registry.add("api.ocr-url") { "http://127.0.0.1:$ocrPort/image-to-text" }
        }
    }
}
