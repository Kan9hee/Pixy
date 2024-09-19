package han.graduate.pixy.dto.adminOnly

import java.time.LocalDateTime

data class CommitInfoDTO(
    val date:LocalDateTime,
    val commitId:String,
    val valAccuracy: Double?,
    val isBuilt:Boolean = false,
    val dockerName:String? = null)
