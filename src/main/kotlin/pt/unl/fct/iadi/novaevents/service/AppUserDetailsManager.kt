package pt.unl.fct.iadi.novaevents.service

import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.provisioning.UserDetailsManager
import org.springframework.stereotype.Service
import pt.unl.fct.iadi.novaevents.repository.UserRepository

@Service
class AppUserDetailsManager(
    val userRepository: UserRepository
) : UserDetailsManager {

    override fun loadUserByUsername(username: String): UserDetails {

        val user = userRepository.findByUsername(username)
            ?: throw UsernameNotFoundException(username)

        val authorities = user.roles.map {
            SimpleGrantedAuthority(it.name)
        }

       return User(
           user.username, user.password,
           user.roles.map { SimpleGrantedAuthority(it.name) })
    }

    override fun createUser(user: UserDetails?) {
            TODO("Not yet implemented")
    }

    override fun updateUser(user: UserDetails?) {
        TODO("Not yet implemented")
    }

    override fun deleteUser(username: String?) {
        TODO("Not yet implemented")
    }

    override fun changePassword(oldPassword: String?, newPassword: String?) {
        TODO("Not yet implemented")
    }

    override fun userExists(username: String): Boolean = userRepository.existsByUsername(username)
}