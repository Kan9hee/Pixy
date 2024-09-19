package han.graduate.pixy.repository

import han.graduate.pixy.entity.UserInfo
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface UserRepository:JpaRepository<UserInfo,Long> {
    fun findByUserName(userName:String): Optional<UserInfo>
}