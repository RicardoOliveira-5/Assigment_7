package pt.unl.fct.iadi.novaevents.repository
import org.springframework.data.jpa.repository.JpaRepository
import pt.unl.fct.iadi.novaevents.model.AppUser

interface UserRepository : JpaRepository<AppUser, Long> {
    fun findByUsername(username: String): AppUser?
    fun existsByUsername(username: String): Boolean

}