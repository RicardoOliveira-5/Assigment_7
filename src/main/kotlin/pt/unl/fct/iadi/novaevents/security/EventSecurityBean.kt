package pt.unl.fct.iadi.novaevents.security

import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component
import pt.unl.fct.iadi.novaevents.repository.EventRepository

@Component("eventSecurity")
class EventSecurityBean(private val eventRepository: EventRepository) {
    fun isOwner(eventId: Long, authentication: Authentication): Boolean {
        val event = eventRepository.findById(eventId).orElse(null) ?: return false
        return event.owner?.username == authentication.name
    }
}