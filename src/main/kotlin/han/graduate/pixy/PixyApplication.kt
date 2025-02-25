package han.graduate.pixy

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration
import org.springframework.boot.runApplication

@SpringBootApplication(exclude = [SecurityAutoConfiguration::class])
class PixyApplication

fun main(args: Array<String>) {
	runApplication<PixyApplication>(*args)
}
