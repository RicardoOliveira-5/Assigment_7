package pt.unl.fct.iadi.novaevents.security

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
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
import org.springframework.security.web.savedrequest.CookieRequestCache
import pt.unl.fct.iadi.novaevents.repository.EventRepository
import pt.unl.fct.iadi.novaevents.repository.UserRepository
import pt.unl.fct.iadi.novaevents.service.AppUserDetailsManager

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
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
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .securityContext { it.securityContextRepository(RequestAttributeSecurityContextRepository()) }
            .requestCache { it.requestCache(CookieRequestCache()) }
            .csrf { csrf ->
                csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                csrf.csrfTokenRequestHandler(CsrfTokenRequestAttributeHandler())
            }
            .authorizeHttpRequests { auth ->
                auth
                    // Públicos: leitura
                    .requestMatchers(HttpMethod.GET, "/clubs", "/clubs/{id}").permitAll()
                    .requestMatchers(HttpMethod.GET, "/events", "/clubs/*/events", "/clubs/*/events/{id}").permitAll()
                    // Login page
                    .requestMatchers("/login").permitAll()
                    // Criar/editar eventos: EDITOR ou ADMIN
                    .requestMatchers(HttpMethod.GET, "/clubs/*/events/new").hasAnyRole("EDITOR", "ADMIN")
                    .requestMatchers(HttpMethod.POST, "/clubs/*/events").hasAnyRole("EDITOR", "ADMIN")
                    .requestMatchers(HttpMethod.GET, "/clubs/*/events/*/edit").hasAnyRole("EDITOR", "ADMIN")
                    .requestMatchers(HttpMethod.PUT, "/clubs/*/events/*").hasAnyRole("EDITOR", "ADMIN")
                    // Apagar: ADMIN
                    .requestMatchers(HttpMethod.GET, "/clubs/*/events/*/delete").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.DELETE, "/clubs/*/events/*").hasRole("ADMIN")
                    // Resto: autenticado
                    .anyRequest().authenticated()
            }
            .formLogin { form ->
                form.loginPage("/login").permitAll()
                form.successHandler(jwtAuthSuccessHandler)
            }
            .logout { logout ->
                logout.logoutUrl("/logout")
                logout.logoutSuccessUrl("/")
                logout.deleteCookies("jwt")
            }
            .addFilterBefore(jwtCookieAuthFilter, UsernamePasswordAuthenticationFilter::class.java)

        return http.build()
    }}