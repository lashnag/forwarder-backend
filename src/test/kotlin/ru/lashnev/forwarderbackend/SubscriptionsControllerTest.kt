package ru.lashnev.forwarderbackend

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import ru.lashnev.forwarderbackend.dto.SubscriptionRawDto
import kotlin.test.assertEquals

@AutoConfigureMockMvc
class SubscriptionsControllerTest : BaseIT() {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Test
    @Sql(scripts = ["/sql/subscriptions.sql"])
    fun getAllSubscriptions() {
        val responseString = mockMvc.perform(
            get("/api/subscriptions").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk)
            .andReturn()
            .response.contentAsString

        val setDtotypeReference: TypeReference<Set<SubscriptionRawDto>> = object : TypeReference<Set<SubscriptionRawDto>>() {}
        val response = objectMapper.readValue(responseString, setDtotypeReference)
        assertEquals(4, response.size)
    }
}