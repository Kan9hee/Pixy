package han.graduate.pixy.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Table(name = "usageInfo")
@Entity
class UsageInfo(userName:String,
                usedDateTime: LocalDateTime,
                gcsVoiceUri:String?,
                isSuccess:Boolean,
                answeredSentences:String?,
                modifiedSentences:String?) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id:Long=0

    @Column
    val userName:String=userName

    @Column
    val usedDateTime:LocalDateTime=usedDateTime

    @Column
    val gcsVoiceUri:String?=gcsVoiceUri

    @Column
    val isSuccess:Boolean=isSuccess

    @Column
    val answeredSentences:String?=answeredSentences

    @Column
    val modifiedSentences:String?=modifiedSentences
}