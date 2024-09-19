package han.graduate.pixy.component

import han.graduate.pixy.config.JwtTokenProvider
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletException
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.HttpServletRequest
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.GenericFilterBean
import java.io.IOException

@Component
class JwtAuthenticationFilter(private val jwtTokenProvider: JwtTokenProvider): GenericFilterBean() {

    @Throws(IOException::class, ServletException::class)
    override fun doFilter(request: ServletRequest, response: ServletResponse?, chain: FilterChain) {
        val httpRequest = request as HttpServletRequest
        val token = resolveToken(httpRequest)

        if (token != null && jwtTokenProvider.validateToken(token)) {
            val authentication = jwtTokenProvider.getAuthentication(token)
            SecurityContextHolder.getContext().authentication = authentication
        }

        chain.doFilter(request, response)
    }

    private fun resolveToken(request: HttpServletRequest): String? {
        val header = request.getHeader("Authorization")
        return header?.takeIf { it.startsWith("Bearer ") }?.substring(7)
    }
}
