package pt.unl.fct.iadi.novaevents.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.OneToMany

@Entity
class AppUser(

    @Id
    @GeneratedValue
    var id: Long? = null,

    @Column(unique = true)
    var username: String,

    var password: String,

    @OneToMany(mappedBy = "appUser", fetch = FetchType.EAGER)
    var roles: MutableList<Role> = mutableListOf()
) {
    constructor() : this(null, "", "")
}