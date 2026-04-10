package pt.unl.fct.iadi.novaevents.model

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne

@Entity
class Role(

    @Id
    @GeneratedValue
    var id: Long? = null,

    var name: String,

    @ManyToOne
    var appUser: AppUser
){
    constructor() : this(null, "", AppUser())
}