package pt.unl.fct.iadi.novaevents.client

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.client.RestClient
import org.springframework.web.client.support.RestClientAdapter
import org.springframework.web.service.invoker.HttpServiceProxyFactory

@Configuration
class OpenWeatherClientConfig(
    private val builder: RestClient.Builder
) {
    private val log = LoggerFactory.getLogger(OpenWeatherClientConfig::class.java)

    @Value("\${weather.api.key}")
    lateinit var apiKey: String

    @Bean
    fun openWeatherClient(): OpenWeatherClient {
        val restClient = builder
            .baseUrl("https://api.openweathermap.org")
            .requestInterceptor { request, body, execution ->
                val principal = SecurityContextHolder.getContext().authentication?.name
                    ?: "anonymous"
                val response = execution.execute(request, body)
                log.info(
                    "[{}] [External API] {} {} [{}]",
                    principal, request.method, request.uri, response.statusCode
                )
                response
            }
            .build()

        val factory = HttpServiceProxyFactory
            .builderFor(RestClientAdapter.create(restClient))
            .build()

        return factory.createClient(OpenWeatherClient::class.java)
    }
}