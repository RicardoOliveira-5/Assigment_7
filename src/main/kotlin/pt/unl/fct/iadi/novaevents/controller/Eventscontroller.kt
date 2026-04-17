package pt.unl.fct.iadi.novaevents.controller

import jakarta.validation.Valid
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Controller
import org.springframework.ui.ModelMap
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.*
import pt.unl.fct.iadi.novaevents.controller.dto.EventForm
import pt.unl.fct.iadi.novaevents.controller.dto.EventUpdateForm
import pt.unl.fct.iadi.novaevents.controller.dto.EventsResponse

import pt.unl.fct.iadi.novaevents.model.Event
import pt.unl.fct.iadi.novaevents.service.ClubsService
import pt.unl.fct.iadi.novaevents.service.EventsService
import pt.unl.fct.iadi.novaevents.service.LocationRequiredException
import pt.unl.fct.iadi.novaevents.service.RainingAtLocationException
import java.time.LocalDate
@Controller
class Eventscontroller(
    private val eventsService: EventsService,
    private val clubService: ClubsService
) {

    @GetMapping("/events")
    fun allEvents(
        @RequestParam(required = false) type: String?,
        @RequestParam(required = false) clubId: Long?,
        @RequestParam(required = false) start: LocalDate?,
        @RequestParam(required = false) end: LocalDate?,
        model: ModelMap
    ): String {
        val typeId = type?.let { eventsService.findTypeByName(it)?.id }
        val events = eventsService.getEventsForClub(typeId, clubId, start, end).map {
            EventsResponse(
                id = it.id,
                name = it.name,
                clubId = it.club!!.id,
                clubName = it.club!!.name,
                date = it.date,
                type = it.type!!,
                location = it.location
            )
        }
        model["events"] = events
        return "events/listEvents"
    }

    @GetMapping("/clubs/{id}/events/new")
    fun showCreateEventForm(@PathVariable id: Long, model: ModelMap): String {
        val club = clubService.clubDetails(id)
        model["club"] = club
        model["eventForm"] = EventForm()
        model["eventTypes"] = eventsService.getAllEventsTypes()
        return "events/form"
    }

    @PostMapping("/clubs/{id}/events")
    fun createEvent(
        @PathVariable id: Long,
        @Valid @ModelAttribute("eventForm") form: EventForm,
        bindingResult: BindingResult,
        model: ModelMap,
        authentication: Authentication
    ): String {

        fun reRenderForm(): String {
            model["club"] = clubService.clubDetails(id)
            model["eventTypes"] = eventsService.getAllEventsTypes()
            return "events/form"
        }

        if (bindingResult.hasErrors()) return reRenderForm()

        if (eventsService.nameExists(form.name!!)) {
            bindingResult.rejectValue("name", "duplicate", "An event with this name already exists")
            return reRenderForm()
        }

        val typeId = form.type!!.toLongOrNull()
            ?: eventsService.findTypeByName(form.type!!)?.id
            ?: eventsService.createEventType(form.type!!).id
            ?: throw IllegalArgumentException("Unknown event type: ${form.type}")

        return try {
            val created = eventsService.createEvent(
                clubId = id,
                name = form.name,
                typeId = typeId,
                date = form.date!!,
                location = form.location,
                description = form.description,
                ownerUserName = authentication.name
            )
            "redirect:/clubs/$id/events/${created.id}"

        } catch (e: LocationRequiredException) {
            bindingResult.rejectValue("location", "required", e.message!!)
            reRenderForm()

        } catch (e: RainingAtLocationException) {
            bindingResult.rejectValue("location", "raining", e.message!!)
            reRenderForm()
        }
    }

    @PreAuthorize("@eventSecurity.isOwner(#eventId, authentication.name) or hasRole('ADMIN')")
    @GetMapping("/clubs/{id}/events/{eventId}/edit")
    fun showEditEventForm(@PathVariable id: Long, @PathVariable eventId: Long, model: ModelMap): String {
        val event = eventsService.findById(eventId)
        val club = clubService.clubDetails(id)

        val eventForm = EventForm(
            name = event.name,
            date = event.date,
            type = event.type?.id?.toString(),
            location = event.location,
            description = event.description
        )
        model["club"] = club
        model["event"] = event
        model["eventForm"] = eventForm
        model["eventUpdateForm"] = EventUpdateForm(
            name = event.name,
            date = event.date,
            type = event.type?.name,
            location = event.location,
            description = event.description
        )
        model["eventTypes"] = eventsService.getAllEventsTypes()
        return "events/editForm"
    }

    @PreAuthorize("@eventSecurity.isOwner(#eventId, authentication.name) or hasRole('ADMIN')")
    @PutMapping("/clubs/{id}/events/{eventId}")
    fun editEvent(
        @PathVariable id: Long,
        @PathVariable eventId: Long,
        @Valid @ModelAttribute("eventUpdateForm") form: EventUpdateForm,
        bindingResult: BindingResult,
        model: ModelMap
    ): String {
        if (bindingResult.hasErrors()) {
            model["club"] = clubService.clubDetails(id)
            model["event"] = eventsService.findById(eventId)
            model["eventTypes"] = eventsService.getAllEventsTypes()
            return "events/editForm"
        }
        if (eventsService.nameEditExists(form.name!!, eventId)) {
            bindingResult.rejectValue("name", "duplicate", "An event with this name already exists")
            model["club"] = clubService.clubDetails(id)
            model["event"] = eventsService.findById(eventId)
            model["eventTypes"] = eventsService.getAllEventsTypes()
            return "events/editForm"
        }

        val event = eventsService.findById(eventId)

        val newType = form.type?.toLongOrNull()
            ?.let { eventsService.getAllEventsTypes().find { t -> t.id == it } }
            ?: form.type?.let { eventsService.findTypeByName(it) }
            ?: event.type

        val updatedEvent = Event(
            id = event.id,
            name = form.name ?: event.name,
            date = form.date ?: event.date,
            type = newType,
            club = event.club,
            location = form.location ?: event.location,
            description = form.description ?: event.description
        )
        eventsService.updateEvent(updatedEvent)
        return "redirect:/clubs/$id/events/${updatedEvent.id}"
    }

    @GetMapping("/clubs/{id}/events/{eventId}")
    fun eventDetails(@PathVariable id: Long, @PathVariable eventId: Long, model: ModelMap): String {
        val event = eventsService.findById(eventId)
        val eventResponse = EventsResponse(
            id = event.id,
            name = event.name,
            clubId = event.club!!.id,
            clubName = event.club!!.name,
            date = event.date,
            type = event.type!!,
            location = event.location
        )
        model["event"] = eventResponse
        return "events/details"
    }

    @PreAuthorize("@eventSecurity.isOwner(#eventId, authentication.name) or hasRole('ADMIN')")
    @DeleteMapping("/clubs/{id}/events/{eventId}")
    fun deleteEvent(@PathVariable eventId: Long, @PathVariable id: Long): String {
        eventsService.deleteEvent(eventId)
        return "redirect:/clubs/$id"
    }

    @PreAuthorize("@eventSecurity.isOwner(#eventId, authentication.name) or hasRole('ADMIN')")
    @GetMapping("/clubs/{id}/events/{eventId}/delete")
    fun showDeleteConfirmation(@PathVariable eventId: Long, model: ModelMap): String {
        val event: Event = eventsService.findById(eventId)
        val eventResponse = EventsResponse(
            id = event.id,
            name = event.name,
            clubId = event.club!!.id,
            clubName = event.club!!.name,
            date = event.date,
            type = event.type!!,
            location = event.location
        )
        model.addAttribute("event", eventResponse)
        return "events/delete"
    }
}