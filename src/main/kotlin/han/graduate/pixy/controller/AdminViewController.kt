package han.graduate.pixy.controller

import han.graduate.pixy.config.stringConfigs.ControllerPathConfig
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("\${path.admin.viewMapping.root}")
class AdminViewController(private val controllerPathConfig: ControllerPathConfig) {
    @GetMapping
    fun adminPage():String{
        return controllerPathConfig.admin.pageFile.root
    }

    @GetMapping("\${path.admin.viewMapping.commitManage}")
    fun commitManagePage():String{
        return controllerPathConfig.admin.pageFile.commitManage
    }

    @GetMapping("\${path.admin.viewMapping.dockerManage}")
    fun dockerManagePage():String{
        return controllerPathConfig.admin.pageFile.dockerManage
    }

    @GetMapping("\${path.admin.viewMapping.usageStatus}")
    fun usageStatusPage():String{
        return controllerPathConfig.admin.pageFile.usageStatus
    }
}