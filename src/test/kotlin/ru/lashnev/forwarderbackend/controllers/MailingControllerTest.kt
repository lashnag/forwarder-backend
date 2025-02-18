package ru.lashnev.forwarderbackend.controllers

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import ru.lashnev.forwarderbackend.BaseIT
import ru.lashnev.forwarderbackend.dto.Mail
import ru.lashnev.forwarderbackend.utils.SendTextUtilService

@AutoConfigureMockMvc
class MailingControllerTest : BaseIT() {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockBean
    private lateinit var sendTextUtilService: SendTextUtilService

    @Test
    @Sql("/sql/subscribers.sql")
    @WithMockUser(username = "admin")
    fun testSendSingleSubscriber() {
        val request = Mail("Some mail text", "lashnag")
        mockMvc
            .perform(
                post("/mail")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)),
            ).andExpect(status().isOk)

        Thread.sleep(ASYNC_DELAY)
        verify(sendTextUtilService, times(1)).sendText(any<Long>(), any<String>(), anyOrNull(), anyOrNull())
    }

    @Test
    @Sql("/sql/subscribers.sql")
    @WithMockUser(username = "admin")
    fun testSendAllSubscribers() {
        val request = Mail("Some mail text")
        mockMvc
            .perform(
                post("/mail")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)),
            ).andExpect(status().isOk)

        Thread.sleep(ASYNC_DELAY)
        verify(sendTextUtilService, times(2)).sendText(any<Long>(), any<String>(), anyOrNull(), anyOrNull())
    }

    @Test
    @Sql("/sql/subscribers.sql")
    @WithMockUser(username = "admin")
    fun dontStopMailingIfOneError() {
        whenever(sendTextUtilService.sendText(any<Long>(), any<String>(), anyOrNull(), anyOrNull())).thenThrow(RuntimeException::class.java)
        val request = Mail("Some mail text")
        mockMvc
            .perform(
                post("/mail")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)),
            ).andExpect(status().isOk)

        Thread.sleep(ASYNC_DELAY)
        verify(sendTextUtilService, times(2)).sendText(any<Long>(), any<String>(), anyOrNull(), anyOrNull())
    }

    @Test
    fun mailingHasAuth() {
        mockMvc
            .perform(
                get("/mail").accept(MediaType.APPLICATION_JSON),
            ).andExpect(status().is4xxClientError)
    }

    companion object {
        private const val ASYNC_DELAY = 500L
    }
}
