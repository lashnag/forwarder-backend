package ru.lashnev.forwarderbackend.services

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.Mockito.any
import org.mockito.Mockito.anySet
import org.mockito.Mockito.anyString
import org.mockito.Mockito.eq
import org.mockito.Mockito.anyLong
import org.mockito.Mockito.times
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.test.context.jdbc.Sql
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate
import ru.lashnev.forwarderbackend.BaseIT
import ru.lashnev.forwarderbackend.messagefetcher.dto.MessageFetcherResponse
import ru.lashnev.forwarderbackend.utils.SendTextUtilService

class MessageForwarderServiceTest : BaseIT() {
    @Autowired
    private lateinit var messageForwarderService: MessageForwarderService

    @MockBean
    private lateinit var restTemplate: RestTemplate

    @MockBean
    private lateinit var sendTextUtilService: SendTextUtilService

    @MockBean
    private lateinit var messageCheckerService: MessageCheckerService

    @Test
    @Sql("/sql/subscriptions.sql")
    fun messageFetcherResponseError() {
        `when`(restTemplate.getForEntity(anyString(), eq(MessageFetcherResponse::class.java))).thenThrow(
            RestClientException("Server Error")
        )

        messageForwarderService.processMessages()

        verify(messageCheckerService, times(0)).containKeyword(anyString(), anySet())
        verify(sendTextUtilService, times(0)).sendText(anyLong(), anyString(), any())
        groupsDao.getValidGroups().forEach {
            assertThat(it.lastMessageId).isEqualTo(0)
        }
    }

    @Test
    @Sql("/sql/subscriptions.sql")
    fun messageFetcherResponseGroupInvalid() {
        `when`(restTemplate.getForEntity(anyString(), eq(MessageFetcherResponse::class.java))).thenThrow(
            HttpClientErrorException(HttpStatus.FORBIDDEN)
        )

        messageForwarderService.processMessages()

        verify(messageCheckerService, times(0)).containKeyword(anyString(), anySet())
        verify(sendTextUtilService, times(0)).sendText(anyLong(), anyString(), any())
        groupsDao.getValidGroups().forEach {
            assertThat(it.invalid).isTrue()
        }
    }

    @Test
    @Sql("/sql/subscriptions.sql")
    fun messageFetcherResponseOkButSubscribersV1() {
        `when`(restTemplate.getForEntity(anyString(), eq(MessageFetcherResponse::class.java))).thenReturn(
            ResponseEntity.ok(
                MessageFetcherResponse(
                    linkedMapOf(
                        9L to "Пересылаемое сообщение 1",
                        10L to "Пересылаемое сообщение 2"
                    ),
                )
            )
        )
        `when`(messageCheckerService.containKeyword(anyString(), anySet())).thenReturn(true)

        messageForwarderService.processMessages()

        verify(messageCheckerService, times(0)).containKeyword(anyString(), anySet())
        verify(sendTextUtilService, times(0)).sendText(anyLong(), anyString(), any())
        groupsDao.getValidGroups().forEach {
            assertThat(it.lastMessageId).isEqualTo(10)
        }
    }

    @Test
    @Sql("/sql/subscriptions_v2.sql")
    fun messageFetcherResponseOk() {
        `when`(restTemplate.getForEntity(anyString(), eq(MessageFetcherResponse::class.java))).thenReturn(
            ResponseEntity.ok(
                MessageFetcherResponse(
                    linkedMapOf(
                        9L to "Пересылаемое сообщение 1",
                        10L to "Пересылаемое сообщение 2"
                    ),
                )
            )
        )
        `when`(messageCheckerService.containKeyword(anyString(), anySet())).thenReturn(true)

        messageForwarderService.processMessages()

        verify(messageCheckerService, times(4)).containKeyword(anyString(), anySet())
        verify(sendTextUtilService, times(4)).sendText(anyLong(), anyString(), any())
        groupsDao.getValidGroups().forEach {
            assertThat(it.lastMessageId).isEqualTo(10)
        }
    }
}