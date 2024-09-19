package han.graduate.pixy.dto

import java.time.LocalDateTime

data class UsageInfoDTO(val userName:String,
                        val usedDateTime:LocalDateTime,
                        val gcsVoiceUri:String?,
                        val isSuccess:Boolean,
                        val answeredSentences:String?,
                        val modifiedSentences:String?)