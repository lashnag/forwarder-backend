package ru.lashnev.forwarderbackend.services

import com.pengrad.telegrambot.request.SendMessage
import com.pengrad.telegrambot.response.SendResponse
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.mock.mockito.SpyBean
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.test.context.jdbc.Sql
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate
import ru.lashnev.forwarderbackend.BaseIT
import ru.lashnev.forwarderbackend.dto.Message
import ru.lashnev.forwarderbackend.dto.MessageFetcherResponse
import ru.lashnev.forwarderbackend.models.Properties
import ru.lashnev.forwarderbackend.utils.SendTextUtilService
import kotlin.test.assertEquals

class MessageForwarderServiceTest : BaseIT() {
    @Autowired
    private lateinit var messageForwarderService: MessageForwarderService

    @MockBean
    private lateinit var restTemplate: RestTemplate

    @SpyBean
    private lateinit var sendTextUtilService: SendTextUtilService

    @MockBean
    private lateinit var messageCheckerService: MessageCheckerService

    @Test
    @Sql("/sql/subscriptions.sql")
    fun messageFetcherResponseError() {
        whenever(restTemplate.getForEntity(any<String>(), eq(MessageFetcherResponse::class.java))).thenThrow(
            RestClientException("Server Error")
        )

        messageForwarderService.processMessages()

        verifyNoInteractions(messageCheckerService)
        verifyNoInteractions(sendTextUtilService)
        groupsDao.getValidGroups().forEach {
            assertThat(it.lastMessageId).isEqualTo(0)
        }
    }

    @Test
    @Sql("/sql/subscriptions.sql")
    fun messageFetcherResponseGroupInvalid() {
        whenever(restTemplate.getForEntity(any<String>(), eq(MessageFetcherResponse::class.java))).thenThrow(
            HttpClientErrorException(HttpStatus.FORBIDDEN)
        )

        messageForwarderService.processMessages()

        verifyNoInteractions(messageCheckerService)
        verifyNoInteractions(sendTextUtilService)
        groupsDao.getValidGroups().forEach {
            assertThat(it.invalid).isTrue()
        }
    }

    @Test
    @Sql("/sql/subscriptions.sql")
    fun messageFetcherResponseOkButSubscribersV1() {
        whenever(restTemplate.getForEntity(any<String>(), eq(MessageFetcherResponse::class.java))).thenReturn(
            ResponseEntity.ok(
                MessageFetcherResponse(
                    linkedMapOf(
                        9L to Message("Пересылаемое сообщение 1"),
                        10L to Message("Пересылаемое сообщение 2")
                    ),
                )
            )
        )
        whenever(messageCheckerService.doesMessageFit(any<String>(), any<Properties>())).thenReturn(true)

        messageForwarderService.processMessages()

        verifyNoInteractions(sendTextUtilService)
        groupsDao.getValidGroups().forEach {
            assertThat(it.lastMessageId).isEqualTo(10)
        }
    }

    @Test
    @Sql("/sql/subscriptions_v2.sql")
    fun messageFetcherResponseOk() {
        whenever(restTemplate.getForEntity(any<String>(), eq(MessageFetcherResponse::class.java))).thenReturn(
            ResponseEntity.ok(
                MessageFetcherResponse(
                    linkedMapOf(
                        9L to Message("Пересылаемое сообщение 1"),
                        10L to Message("Пересылаемое сообщение 2")
                    ),
                )
            )
        )
        whenever(messageCheckerService.doesMessageFit(any<String>(), any<Properties>())).thenReturn(true)

        messageForwarderService.processMessages()

        verify(messageCheckerService, times(4)).doesMessageFit(any<String>(), any<Properties>())
        verify(sendTextUtilService, times(4)).sendText(any<Long>(), any<String>(), anyOrNull())
        groupsDao.getValidGroups().forEach {
            assertThat(it.lastMessageId).isEqualTo(10)
        }
    }

    @Test
    @Sql("/sql/subscriptions_v2.sql")
    fun userBlockBot() {
        val messageResponse = mock<SendResponse>()
        whenever(messageResponse.errorCode()).thenReturn(403)
        whenever(telegramBot.execute(any<SendMessage>())).thenReturn(messageResponse)
        whenever(restTemplate.getForEntity(any<String>(), eq(MessageFetcherResponse::class.java))).thenReturn(
            ResponseEntity.ok(MessageFetcherResponse(linkedMapOf(9L to Message("Пересылаемое сообщение 1"))))
        )
        whenever(messageCheckerService.doesMessageFit(any<String>(), any<Properties>())).thenReturn(true)

        messageForwarderService.processMessages()

        assertEquals(0, subscriptionDao.getAll().filter { it.subscriber.chatId != null }.size)
    }
}