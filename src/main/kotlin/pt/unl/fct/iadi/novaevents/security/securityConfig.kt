package pt.unl.fct.iadi.novaevents.security

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.security.web.context.RequestAttributeSecurityContextRepository
import org.springframework.security.web.csrf.CookieCsrfTokenRepository
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler
import pt.unl.fct.iadi.novaevents.repository.EventRepository
import pt.unl.fct.iadi.novaevents.repository.UserRepository
import pt.unl.fct.iadi.novaevents.service.AppUserDetailsManager

@Configuration
@EnableWebSecurity
class SecurityConfig (
    private val jwtCookieAuthFilter     : JwtCookieAuthFilter,
    private val appUserRepository: UserRepository,
    private val jwtAuthSuccessHandler: JwtAuthSuccessHandler
){
    @Bean
    fun passwordEncoder(): PasswordEncoder =
        BCryptPasswordEncoder()
    @Bean
    fun userDetailsService(encoder: PasswordEncoder) = AppUserDetailsManager(appUserRepository)
    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers(HttpMethod.GET, "/posts", "/posts/**").permitAll()
                    .requestMatchers(HttpMethod.POST, "/posts").hasAnyRole("EDITOR", "ADMIN")
                    .requestMatchers(HttpMethod.PUT, "/posts/**").hasAnyRole("EDITOR", "ADMIN")
                    .requestMatchers(HttpMethod.DELETE, "/posts/**").hasRole("ADMIN")
                    .anyRequest().authenticated()
            }
            .httpBasic {}
            .oauth2Login { }

        // form based login
        http
            .authorizeHttpRequests { auth ->
                auth.anyRequest().authenticated()
            }
            .formLogin { }
// cookies wirh JWT
        http
            .sessionManagement {
                it.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }
            .addFilterBefore(jwtCookieAuthFilter,
                UsernamePasswordAuthenticationFilter::class.java)
            .authorizeHttpRequests { }
            .formLogin { form ->
                form.loginPage("/login").permitAll()
                form.successHandler(jwtAuthSuccessHandler)
            }


        http
            .sessionManagement {
                it.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
            }

        http
            .sessionManagement {
                it.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }
            .securityContext {
                it.securityContextRepository(RequestAttributeSecurityContextRepository())
            }
            .csrf { csrf ->
                csrf.csrfTokenRepository(CookieCsrfTokenRepository())
                csrf.csrfTokenRequestHandler(CsrfTokenRequestAttributeHandler())
            }
        return http.build()
    }
}