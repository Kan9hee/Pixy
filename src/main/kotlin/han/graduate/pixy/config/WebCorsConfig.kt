package han.graduate.pixy.config

import han.graduate.pixy.config.stringConfigs.SecurityStringConfig
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebCorsConfig(private val securityStringConfig: SecurityStringConfig) : WebMvcConfigurer {
    override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping(securityStringConfig.cors.mapping)
            .allowedOrigins(securityStringConfig.cors.origin)
            .allowedMethods(*securityStringConfig.cors.methods.toTypedArray())
            .allowedHeaders(securityStringConfig.cors.headers)
    }
}