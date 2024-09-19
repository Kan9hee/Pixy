package han.graduate.pixy.controller

import han.graduate.pixy.config.stringConfigs.ControllerPathConfig
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

@Controller
class GeneralViewController(private val controllerPathConfig: ControllerPathConfig) {
    @GetMapping("\${path.open.viewMapping.login}")
    fun loginPage():String{
        return controllerPathConfig.open.pageFile.login
    }

    @GetMapping("\${path.open.viewMapping.main}")
    fun mainPage():String{
        return controllerPathConfig.open.pageFile.main
    }

    @GetMapping("\${path.open.viewMapping.recordPage}")
    fun recordPage():String{
        return controllerPathConfig.open.pageFile.recordPage
    }
}