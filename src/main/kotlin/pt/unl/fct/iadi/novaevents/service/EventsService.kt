package pt.unl.fct.iadi.novaevents.service

import jakarta.persistence.EntityNotFoundException
import org.springframework.stereotype.Service
import pt.unl.fct.iadi.novaevents.model.AppUser
import pt.unl.fct.iadi.novaevents.model.Event
import pt.unl.fct.iadi.novaevents.model.EventType
import pt.unl.fct.iadi.novaevents.repository.ClubRepository
import pt.unl.fct.iadi.novaevents.repository.EventRepository
import pt.unl.fct.iadi.novaevents.repository.EventTypeRepository
import pt.unl.fct.iadi.novaevents.repository.UserRepository
import java.time.LocalDate

class LocationRequiredException(message: String) : RuntimeException(message)
class RainingAtLocationException(message: String) : RuntimeException(message)
@Service
class EventsService(
    private val eventRepository: EventRepository,
    private val clubRepository: ClubRepository,
    private val userRepository: UserRepository,
    private val eventTypeRepository: EventTypeRepository
) {
    fun getEventsForClub(
        typeId: Long? = null,
        clubId: Long?,
        start: LocalDate?,
        end: LocalDate?
    ): List<Event> {
        var events = eventRepository.findAllWithDetails()

        if (clubId != null) events = events.filter { it.club?.id == clubId }
        if (typeId != null) events = events.filter { it.type?.id == typeId }
        if (start != null) events = events.filter { !it.date.isBefore(start) }
        if (end != null) events = events.filter { !it.date.isAfter(end) }

        return events
    }

    fun findById(id: Long): Event {
        return eventRepository.findById(id)
            .orElseThrow { EntityNotFoundException("Event with id $id not found") }
    }

    fun createEvent(
        name: String,
        date: LocalDate,
        typeId: Long,
        clubId: Long,
        location: String? = null,
        description: String? = null,
        ownerUserName: String
    ): Event {
        val owner = userRepository.findByUsername(ownerUserName)
            ?:  { EntityNotFoundException("User not found") }

        val club = clubRepository.findById(clubId)
            .orElseThrow { EntityNotFoundException("Club not found") }

        val type = eventTypeRepository.findById(typeId)
            .orElseThrow { EntityNotFoundException("Event type not found") }

        val event = Event(
            name = name,
            date = date,
            club = club,
            type = type,
            location = location,
            description = description,
            owner = owner as AppUser?
        )

        return eventRepository.save(event)
    }

    fun updateEvent(event: Event) {
        if (!eventRepository.existsById(event.id)) {
            throw EntityNotFoundException("Event with id ${event.id} not found")
        }
        eventRepository.save(event) // save também faz update
    }

    fun deleteEvent(id: Long) {
        if (!eventRepository.existsById(id)) {
            throw EntityNotFoundException("Event with id $id not found")
        }
        eventRepository.deleteById(id)
    }

    fun nameExists(name: String): Boolean {
        return eventRepository.existsByNameIgnoreCase(name)
    }

    fun nameEditExists(name: String, excludeId: Long): Boolean {
        return eventRepository.existsByNameIgnoreCaseAndIdNot(name, excludeId)
    }

    fun getAllEventsTypes(): List<pt.unl.fct.iadi.novaevents.model.EventType> {
        return eventTypeRepository.findAll()
    }

    fun findTypeByName(name: String): pt.unl.fct.iadi.novaevents.model.EventType? {
        return eventTypeRepository.findByNameIgnoreCase(name)
    }

    fun eventsCountForAllClubs(clubIds: List<Long>): Map<Long, Long> {
        val results = eventRepository.countByClubIds(clubIds)
        val countMap = results.associate { row ->
            (row[0] as Long) to (row[1] as Long)
        }
        return clubIds.associateWith { countMap.getOrDefault(it, 0L) }
    }

    fun createEventType(name: String): EventType {
        return eventTypeRepository.save(EventType(name = name))
    }
}