package pt.unl.fct.iadi.novaevents.controller

import org.springframework.stereotype.Controller
import org.springframework.ui.ModelMap

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import pt.unl.fct.iadi.novaevents.service.ClubsService
import pt.unl.fct.iadi.novaevents.service.EventsService


@Controller
@RequestMapping("/clubs")
class ClubsController(private val clubService: ClubsService, val eventsService: EventsService) {

    @GetMapping()
    fun listClubs(model: ModelMap): String {
        val clubs = clubService.getAllClubs()
        val eventsCount = clubs.associate { it.id to it.events.size.toLong() }
        model["clubs"] = clubs
        model["eventsCount"] = eventsCount
        return "clubs/listClubs"
    }
    @GetMapping("/{id}")
    fun clubDetails(@PathVariable id: Long, model: ModelMap): String {
        val club = clubService.clubDetails(id)
        val events = eventsService.getEventsForClub( clubId = id, typeId = null, start = null, end = null)
        model["club"] = club
        model["events"] = events
        return "clubs/detail"
    }



}