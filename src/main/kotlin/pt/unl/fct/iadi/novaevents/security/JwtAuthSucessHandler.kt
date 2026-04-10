package pt.unl.fct.iadi.novaevents.security

import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import org.springframework.security.web.context.RequestAttributeSecurityContextRepository
import org.springframework.security.web.savedrequest.CookieRequestCache
import org.springframework.stereotype.Component

@Component
class JwtAuthSuccessHandler(private val jwtService: JwtService) : AuthenticationSuccessHandler {

    private val securityContextRepository = RequestAttributeSecurityContextRepository()
    private val requestCache = CookieRequestCache()


    override fun onAuthenticationSuccess(
        request: HttpServletRequest?,
        response: HttpServletResponse?,
        authentication: Authentication?
    ) {
        val token = jwtService.generate(authentication!!.name,
            authentication.authorities.map { it.authority })
        val cookie = Cookie("jwt", token).apply {
            isHttpOnly = true
            path = "/"
            maxAge = 3600
        }
        response?.addCookie(cookie)
        val savedRequest = requestCache.getRequest(request, response)
        val redirectUrl = savedRequest?.redirectUrl ?: (request?.contextPath + "/")
        requestCache.removeRequest(request, response)
        response?.sendRedirect(redirectUrl)
    }


}