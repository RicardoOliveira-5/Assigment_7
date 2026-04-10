package pt.unl.fct.iadi.novaevents.security

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.Date
import javax.crypto.SecretKey

@Service
class JwtService(@Value("\${jwt.secret}") secret: String) {
    private val key: SecretKey = Keys.hmacShaKeyFor(secret.toByteArray())
    private val expirationMs = 3_600_000L // 1 hour
    fun generate(name: String, roles: List<String>): String =
        Jwts.builder()
            .subject(name)
            .claim("roles", roles)
            .issuedAt(Date())
            .expiration(Date(System.currentTimeMillis() + expirationMs))
            .signWith(key)
            .compact()


    fun validate(token: String): Map<String, Any>? =
        runCatching {
            val claims: Claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .payload
            @Suppress("UNCHECKED_CAST")
            mapOf("name" to claims.subject, "roles" to claims["roles"] as List<String>)
        }.getOrNull()
}