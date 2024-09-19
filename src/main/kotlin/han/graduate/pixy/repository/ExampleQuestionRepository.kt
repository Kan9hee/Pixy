package han.graduate.pixy.repository

import han.graduate.pixy.entity.ExampleQuestionInfo
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface ExampleQuestionRepository: JpaRepository<ExampleQuestionInfo, Long> {
    @Query("SELECT eq.questionString FROM ExampleQuestionInfo eq WHERE eq.difficulty = :difficulty ORDER BY RAND() LIMIT 1")
    fun getRandomQuestionByDifficulty(@Param("difficulty") difficulty: Int): String
}