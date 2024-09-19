package han.graduate.pixy.repository

import han.graduate.pixy.entity.CommitInfo
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface CommitRepository: JpaRepository<CommitInfo,Long> {

    fun findByCommitId(commitId:String):Optional<CommitInfo>

    @Query("SELECT c.commitId FROM CommitInfo c WHERE c.commitId IN :commitIds")
    fun findExistingCommitIds(@Param("commitIds") commitIds: List<String>): List<String>
}