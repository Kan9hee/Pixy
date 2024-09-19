package han.graduate.pixy.config

import han.graduate.pixy.config.stringConfigs.SecurityStringConfig
import org.springframework.context.annotation.Configuration
import org.springframework.messaging.simp.config.MessageBrokerRegistry
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker
import org.springframework.web.socket.config.annotation.StompEndpointRegistry
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer

@Configuration
@EnableWebSocketMessageBroker
class WebSocketConfig(private val securityStringConfig: SecurityStringConfig) : WebSocketMessageBrokerConfigurer {
    override fun configureMessageBroker(config: MessageBrokerRegistry) {
        config.enableSimpleBroker(securityStringConfig.webSocket.messageBrokerPath)
        config.setUserDestinationPrefix(securityStringConfig.webSocket.messageBrokerPath)
    }

    override fun registerStompEndpoints(registry: StompEndpointRegistry) {
        registry.addEndpoint(securityStringConfig.webSocket.stompEndpoint)
            .setAllowedOriginPatterns(securityStringConfig.webSocket.allowedOriginPattern)
            .withSockJS()
    }
}