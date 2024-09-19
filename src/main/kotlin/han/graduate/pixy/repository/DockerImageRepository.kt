package han.graduate.pixy.repository

import han.graduate.pixy.entity.DockerImageInfo
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface DockerImageRepository:JpaRepository<DockerImageInfo,Long> {

    fun findByIsActivateTrue(): Optional<DockerImageInfo>

    @Modifying
    @Query("UPDATE DockerImageInfo d SET d.isActivate = CASE WHEN d.dockerName = :targetName THEN TRUE ELSE FALSE END")
    fun updateActiveDocker(@Param("targetName") targetName:String)
}