package han.graduate.pixy.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Table(name = "commitInfo")
@Entity
class CommitInfo(
    date: LocalDateTime = LocalDateTime.now(),
    commitId:String,
    valAccuracy: Double?,
    isBuilt:Boolean = false,
    dockerName:String? = null) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id:Long = 0

    @Column
    val date: LocalDateTime = date

    @Column
    val commitId:String = commitId

    @Column
    val valAccuracy:Double? = valAccuracy

    @Column
    var isBuilt:Boolean = isBuilt
        protected set

    @Column
    var dockerName:String? = dockerName
        protected set

    fun changeBuiltInfo(isBuilt: Boolean,dockerImageName: String){
        this.isBuilt = isBuilt
        this.dockerName = dockerImageName
    }
}