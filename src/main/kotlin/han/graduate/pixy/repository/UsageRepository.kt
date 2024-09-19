package han.graduate.pixy.repository

import han.graduate.pixy.entity.UsageInfo
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
interface UsageRepository:JpaRepository<UsageInfo,Long> {
    fun findByUserName(userName: String): UsageInfo

    @Query("SELECT COUNT(u) FROM UsageInfo u WHERE DATE(u.usedDateTime) = :usedDate")
    fun countByUsedDateTime(@Param("usedDate") usedDate: LocalDate): Long
}