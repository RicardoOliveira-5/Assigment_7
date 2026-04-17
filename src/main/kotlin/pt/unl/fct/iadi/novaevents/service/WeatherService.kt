package pt.unl.fct.iadi.novaevents.service
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClientException
import pt.unl.fct.iadi.novaevents.client.OpenWeatherClient

@Service
class WeatherService(
    private val openWeatherClient: OpenWeatherClient
) {
    private val log = LoggerFactory.getLogger(WeatherService::class.java)

    @Value("\${weather.api.key}")
    lateinit var apiKey: String

    /**
     *   true  — se está a chover na localização
     *   false — se não está a chover
     *   null  — se a API não respondeu / localização inválida
     */
    fun isRaining(location: String): Boolean? {
        return try {
            val response = openWeatherClient.getWeather(
                q = location,
                appid = apiKey,
                units = "metric"
            )
            response.weather.any { it.main.equals("Rain", ignoreCase = true) }
        } catch (e: RestClientException) {
            log.warn("[WeatherService] Failed to fetch weather for '{}': {}", location, e.message)
            null
        } catch (e: Exception) {
            log.warn("[WeatherService] Unexpected error for '{}': {}", location, e.message)
            null
        }
    }
}