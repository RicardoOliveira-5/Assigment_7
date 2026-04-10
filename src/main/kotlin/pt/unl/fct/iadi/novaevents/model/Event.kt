package pt.unl.fct.iadi.novaevents.model

import jakarta.persistence.*
import java.time.LocalDate

@Entity
@Table(name = "events")
class Event(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    var name: String = "",

    var date: LocalDate = LocalDate.now(),

    var location: String? = null,

    var description: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "club_id")
    var club: Club ? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "type_id")
    var type: EventType ? = null,

    @ManyToOne()
    var owner: AppUser ? = null


) {
    constructor(): this(0, "", LocalDate.now(), null, null, null, null)


}