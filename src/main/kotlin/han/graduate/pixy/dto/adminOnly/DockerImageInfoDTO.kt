package han.graduate.pixy.dto.adminOnly

import java.time.LocalDateTime

data class DockerImageInfoDTO(val date: LocalDateTime,
                              val dockerName:String,
                              val isActivate:Boolean = false)
