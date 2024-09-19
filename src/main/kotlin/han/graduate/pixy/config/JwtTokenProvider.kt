package han.graduate.pixy.config

import han.graduate.pixy.config.stringConfigs.SecurityStringConfig
import han.graduate.pixy.dto.JwtTokenDTO
import io.jsonwebtoken.*
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import java.util.*
import java.util.stream.Collectors
import javax.crypto.SecretKey


@Configuration
class JwtTokenProvider(@Value("\${jwt.secret}") secretKey: String,
                       private val securityStringConfig: SecurityStringConfig) {
    private val key: SecretKey

    init {
        val keyBytes = Decoders.BASE64.decode(secretKey)
        key = Keys.hmacShaKeyFor(keyBytes)
    }

    fun generateToken(authentication:Authentication):JwtTokenDTO{
        val authorities = authentication
            .authorities
            .stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.joining(securityStringConfig.authoritySplit))

        val now = Date()

        val accessToken = Jwts.builder()
            .subject(authentication.name)
            .issuedAt(now)
            .claim(securityStringConfig.claims,authorities)
            .expiration(Date(now.time+securityStringConfig.token.accessTokenMillisecond.toLong()))
            .signWith(this.key)
            .compact()

        val refreshToken = Jwts.builder()
            .expiration(Date(now.time+securityStringConfig.token.refreshTokenMillisecond.toLong()))
            .signWith(this.key)
            .compact()

        return JwtTokenDTO("Bearer ",accessToken,refreshToken)
    }

    fun getAuthentication(token: String): UsernamePasswordAuthenticationToken {
        val claims = parseClaims(token) ?: throw RuntimeException("권한 정보 없음")
        val authorities = claims["Bearer "]?.toString()?.split(securityStringConfig.authoritySplit)?.map { SimpleGrantedAuthority(it) } ?: emptyList()
        val principal = User(claims.subject, securityStringConfig.userDetailsPassword, authorities)
        return UsernamePasswordAuthenticationToken(principal, securityStringConfig.userDetailsPassword, authorities)
    }

    fun getUserFromRefreshToken(refreshToken: String): UserDetails? {
        return try {
            val claims = parseClaims(refreshToken)
            val authorities = claims?.get(securityStringConfig.claims)?.toString()?.split(securityStringConfig.authoritySplit)?.map { SimpleGrantedAuthority(it) } ?: emptyList()
            User(claims?.subject, securityStringConfig.userDetailsPassword, authorities)
        } catch (e: Exception) {
            null
        }
    }

    fun validateToken(token:String): Boolean {
        return try {
            val claims: Claims = Jwts.parser()
                .verifyWith(this.key)
                .build()
                .parseSignedClaims(token)
                .payload

            val expirationDate = claims.expiration
            if (expirationDate.before(Date()))
                return false

            true
        } catch (e: Exception) {
            false
        }
    }

    private fun parseClaims(accessToken: String): Claims? {
        return try {
            Jwts.parser()
                .verifyWith(this.key)
                .build()
                .parseSignedClaims(accessToken)
                .payload
        } catch (e: Exception) {
            null
        }
    }
}