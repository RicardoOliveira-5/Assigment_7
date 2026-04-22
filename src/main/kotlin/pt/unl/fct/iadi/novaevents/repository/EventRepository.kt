package pt.unl.fct.iadi.novaevents.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import pt.unl.fct.iadi.novaevents.model.Event

interface EventRepository : JpaRepository<Event, Long> {

    fun existsByNameIgnoreCase(name: String): Boolean
    fun existsByNameIgnoreCaseAndIdNot(name: String, id: Long): Boolean

    @Query("SELECT e.club.id, COUNT(e) FROM Event e WHERE e.club.id IN :clubIds GROUP BY e.club.id")
    fun countByClubIds(@Param("clubIds") clubIds: List<Long>): List<Array<Any>>

    @Query("SELECT e FROM Event e LEFT JOIN FETCH e.type LEFT JOIN FETCH e.club")
    fun findAllWithDetails(): List<Event>

    fun findByName(name: String): Event?
}