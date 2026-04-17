package pt.unl.fct.iadi.novaevents.security

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint

class RestAuthenticationEntryPoint : BasicAuthenticationEntryPoint() {
    override fun commence(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authException: AuthenticationException
    ) {
        val acceptHeader = request.getHeader("Accept") ?: ""
        
        if (acceptHeader.contains("application/json")) {
            // For JSON requests, return 401
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized")
        } else {
            // For HTML requests, redirect to login
            response.sendRedirect(request.contextPath + "/login")
        }
    }

    init {
        realmName = "Campus Clubs"
    }
}

