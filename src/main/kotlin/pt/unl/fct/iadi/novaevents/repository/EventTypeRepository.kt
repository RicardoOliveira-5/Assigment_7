package pt.unl.fct.iadi.novaevents.repository

import org.springframework.data.jpa.repository.JpaRepository
import pt.unl.fct.iadi.novaevents.model.EventType

interface EventTypeRepository : JpaRepository<EventType, Long> {
    fun findByNameIgnoreCase(name: String): EventType?
    fun findByName(name: String): MutableList<EventType>
}
