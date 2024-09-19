package han.graduate.pixy.service

import han.graduate.pixy.config.JwtTokenProvider
import han.graduate.pixy.config.stringConfigs.SecurityStringConfig
import han.graduate.pixy.dto.JwtTokenDTO
import han.graduate.pixy.entity.UserInfo
import han.graduate.pixy.repository.UserRepository
import jakarta.servlet.http.HttpServletRequest
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional


@Service
class CustomUserDetailsService(private val userRepository: UserRepository,
                               private val passwordEncoder: PasswordEncoder,
                               private val authenticationManagerBuilder: AuthenticationManagerBuilder,
                               private val jwtTokenProvider: JwtTokenProvider,
                               private val securityStringConfig: SecurityStringConfig): UserDetailsService {

    @Transactional
    fun refreshAccessToken(request: HttpServletRequest): JwtTokenDTO {
        val refreshToken = request.cookies?.firstOrNull { it.name == "refreshToken" }?.value
            ?: throw IllegalArgumentException("Refresh Token이 없습니다.")
        if (!checkValidate(refreshToken)) {
            throw IllegalArgumentException("유효하지 않은 Refresh Token입니다.")
        }

        val userDetails = jwtTokenProvider.getUserFromRefreshToken(refreshToken)
        val authentication = UsernamePasswordAuthenticationToken(userDetails, null, userDetails?.authorities)
        val newAccessToken = jwtTokenProvider.generateToken(authentication).accessToken
        return JwtTokenDTO(securityStringConfig.token.authType, newAccessToken, refreshToken)
    }

    @Transactional
    fun checkValidate(tokenString:String): Boolean {
        return jwtTokenProvider.validateToken(tokenString)
    }

    @Transactional(readOnly = true)
    fun logIn(userName: String, password: String): JwtTokenDTO {
        val authenticationToken = UsernamePasswordAuthenticationToken(userName, password)
        val authentication = authenticationManagerBuilder.`object`.authenticate(authenticationToken)

        return jwtTokenProvider.generateToken(authentication)
    }

    @Throws(UsernameNotFoundException::class)
    override fun loadUserByUsername(username: String): UserDetails? {
        return userRepository.findByUserName(username)
            .map { userInfo: UserInfo -> createUserDetails(userInfo) }
            .orElseThrow { UsernameNotFoundException("해당하는 회원을 찾을 수 없습니다.") }
    }

    private fun createUserDetails(userInfo: UserInfo): UserDetails? {
        return User.builder()
            .username(userInfo.username)
            .password(passwordEncoder.encode(userInfo.password))
            .roles(userInfo.systemLevel)
            .build()
    }
}