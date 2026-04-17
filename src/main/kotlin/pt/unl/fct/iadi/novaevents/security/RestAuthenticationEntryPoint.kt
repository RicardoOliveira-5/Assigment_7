package pt.unl.fct.iadi.novaevents.security

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint

class RestAuthenticationEntryPoint : LoginUrlAuthenticationEntryPoint("/login") {
    override fun commence(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authException: AuthenticationException
    ) {
        val acceptHeader = request.getHeader("Accept") ?: ""
        
        if (acceptHeader.contains("application/json")) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized")
        } else {
            super.commence(request, response, authException)
        }
    }
}

