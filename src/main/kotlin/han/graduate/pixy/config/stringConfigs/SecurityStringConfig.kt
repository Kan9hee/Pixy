package han.graduate.pixy.config.stringConfigs

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "security")
class SecurityStringConfig {
    lateinit var authRequiredPath: String
    lateinit var claims: String
    lateinit var authoritySplit: String
    lateinit var userDetailsPassword: String
    lateinit var token: TokenProperties
    lateinit var cors: CorsProperties
    lateinit var webSocket: WebSocketProperties

    class TokenProperties {
        lateinit var authType: String
        lateinit var accessTokenMillisecond: String
        lateinit var refreshTokenMillisecond: String
    }

    class CorsProperties {
        lateinit var mapping: String
        lateinit var origin: String
        lateinit var headers: String
        lateinit var methods: List<String>
    }

    class WebSocketProperties {
        lateinit var messageBrokerPath: String
        lateinit var stompEndpoint: String
        lateinit var allowedOriginPattern: String
    }
}