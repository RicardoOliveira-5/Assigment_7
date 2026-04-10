package pt.unl.fct.iadi.novaevents.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.context.RequestAttributeSecurityContextRepository
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtCookieAuthFilter(private val jwtService: JwtService) : OncePerRequestFilter() {
    private val securityContextRepository = RequestAttributeSecurityContextRepository()
    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, filterChain: FilterChain) {
        if (SecurityContextHolder.getContext().authentication == null) {
            val token = request.cookies?.find { it.name == "jwt" }?.value
            if (token != null) {
                val claims = jwtService.validate(token)
                if (claims != null) {
                    @Suppress("UNCHECKED_CAST")
                    val authorities = (claims["roles"] as List<String>).map { SimpleGrantedAuthority(it) }
                    val context = SecurityContextHolder.createEmptyContext()
                    context.authentication =
                        UsernamePasswordAuthenticationToken(claims["name"], null, authorities)
                    SecurityContextHolder.setContext(context)
                    securityContextRepository.saveContext(context, request, response)
                }
            }
        }
        filterChain.doFilter(request, response)
    }
}