import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.w3c.dom.events.EventException
import pt.unl.fct.iadi.novaevents.client.OpenWeatherClient
import pt.unl.fct.iadi.novaevents.client.WeatherCondition
import pt.unl.fct.iadi.novaevents.client.WeatherResponse
import pt.unl.fct.iadi.novaevents.controller.dto.EventForm
import pt.unl.fct.iadi.novaevents.model.AppUser
import pt.unl.fct.iadi.novaevents.model.Club
import pt.unl.fct.iadi.novaevents.model.ClubCategory
import pt.unl.fct.iadi.novaevents.model.Event
import pt.unl.fct.iadi.novaevents.model.EventType
import pt.unl.fct.iadi.novaevents.repository.ClubRepository
import pt.unl.fct.iadi.novaevents.repository.EventRepository
import pt.unl.fct.iadi.novaevents.repository.EventTypeRepository
import pt.unl.fct.iadi.novaevents.repository.UserRepository
import pt.unl.fct.iadi.novaevents.service.EventsService
import pt.unl.fct.iadi.novaevents.service.RainingAtLocationException
import pt.unl.fct.iadi.novaevents.service.WeatherService
import java.time.LocalDate
import java.util.Optional

class TestEventService {

    private val clubRepo = mock(ClubRepository::class.java)
    private val eventRepo = mock(EventRepository::class.java)
    private val eventTypeRepo = mock(EventTypeRepository::class.java)
    private val userRepo = mock(UserRepository::class.java)
    private val weatherClient = mock(OpenWeatherClient::class.java)
    private val weatherService = WeatherService(weatherClient)

    private lateinit var service: EventsService

    @BeforeEach
    fun setUp() {
        weatherService.apiKey = "test-key"
        service = EventsService(eventRepo, clubRepo, userRepo, eventTypeRepo, weatherService)
    }

    @Test
    fun `createEvent rejects rainy hiking events`() {
        val club = Club(name = "Hiking & Outdoors Club", category = ClubCategory.SPORTS, description = "Outdoor club")
        val user = AppUser(username = "alice", password = "password")
        val type = EventType(name = "Hiking")  // ✅ Changed from "WORKSHOP" to "Hiking"
        val request = EventForm(
            name = "Rainy Hike",
            date = LocalDate.parse("2026-05-01"),
            location = "Sintra",
            type = "Hiking",
            description = "A hike"
        )

        `when`(userRepo.findByUsername("alice")).thenReturn(user)
        `when`(eventRepo.findByName("Rainy Hike")).thenReturn(null)
        `when`(clubRepo.findById(1L)).thenReturn(Optional.of(club))
        `when`(eventTypeRepo.findById(1L)).thenReturn(Optional.of(type))
        `when`(weatherClient.getWeather("Sintra", "test-key", "metric")).thenReturn(
            WeatherResponse(listOf(WeatherCondition(main = "Rain", description = "light rain")))
        )

        val exception = assertThrows(RainingAtLocationException::class.java) {  // ✅ Changed to RainingAtLocationException
            service.createEvent(
                name = request.name!!,
                date = request.date!!,
                typeId = 1L,
                clubId = 1L,
                location = request.location,
                description = request.description,
                ownerUserName = "alice"
            )
        }

        assertEquals("Cannot create hiking event: it is raining at the location", exception.message)  // ✅ Updated message
    }

    @Test
    fun `createEvent allows hiking events when weather is clear`() {
        val club = Club(name = "Hiking & Outdoors Club", category = ClubCategory.SPORTS, description = "Outdoor club")
        val user = AppUser(username = "alice", password = "password")
        val type = EventType(name = "WORKSHOP")
        val request = EventForm(
            name = "Sunny Hike",
            date = LocalDate.parse("2026-05-01"),
            location = "Sintra",
            type = "WORKSHOP",
            description = "A hike"
        )


        `when`(userRepo.findByUsername("alice")).thenReturn(user)
        `when`(eventRepo.findByName("Sunny Hike")).thenReturn(null)
        `when`(clubRepo.findById(1L)).thenReturn(Optional.of(club))
        `when`(eventTypeRepo.findById(1L)).thenReturn(Optional.of(type))
        `when`(weatherClient.getWeather("Sintra", "test-key", "metric")).thenReturn(
            WeatherResponse(listOf(WeatherCondition(main = "Clear", description = "clear sky")))
        )
        `when`(eventRepo.save(any(Event::class.java))).thenAnswer { invocation ->
            invocation.arguments[0] as Event
        }

        val result = service.createEvent(
            name = request.name!!,
            date = request.date!!,
            typeId = 1L,
            clubId = 1L,
            location = request.location,
            description = request.description,
            ownerUserName = "alice"
        )

        assertEquals("Sunny Hike", result.name)
        assertEquals("Sintra", result.location)
    }
}