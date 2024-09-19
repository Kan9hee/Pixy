package han.graduate.pixy.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Table(name = "dockerImageInfo")
@Entity
class DockerImageInfo(date: LocalDateTime,
                      dockerName:String,
                      isActivate:Boolean=false) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id:Long=0

    @Column
    val date: LocalDateTime = date

    @Column
    val dockerName:String = dockerName

    @Column
    var isActivate:Boolean=isActivate
        protected set

    fun changeActivateStatus(isActivate: Boolean){
        this.isActivate=isActivate
    }
}