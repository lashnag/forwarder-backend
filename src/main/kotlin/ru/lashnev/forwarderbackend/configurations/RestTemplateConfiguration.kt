package ru.lashnev.forwarderbackend.configurations

import org.slf4j.MDC
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpRequest
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.client.ClientHttpResponse
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.DefaultUriBuilderFactory
import ru.lashnev.forwarderbackend.utils.MDCType

@Configuration
class RestTemplateConfiguration {
    @Value("\${rest-template.connection-timeout}")
    private lateinit var connectionTimeout: String

    @Bean
    fun getRestTemplate(): RestTemplate {
        val defaultUriBuilderFactory = DefaultUriBuilderFactory()
        defaultUriBuilderFactory.encodingMode = DefaultUriBuilderFactory.EncodingMode.VALUES_ONLY

        val factory = SimpleClientHttpRequestFactory()
        factory.setConnectTimeout(connectionTimeout.toInt())
        factory.setReadTimeout(connectionTimeout.toInt())

        return RestTemplate(factory).apply {
            setUriTemplateHandler(defaultUriBuilderFactory)
            interceptors.add(MdcRequestInterceptor())
        }
    }

    private class MdcRequestInterceptor : ClientHttpRequestInterceptor {
        override fun intercept(
            request: HttpRequest,
            body: ByteArray,
            execution: ClientHttpRequestExecution
        ): ClientHttpResponse {
            MDCType.entries.forEach {
                val value = MDC.get(it.value)
                if (!value.isNullOrBlank()) {
                    request.headers["custom-${it.value}"] = value
                }
            }
            return execution.execute(request, body)
        }
    }
}
