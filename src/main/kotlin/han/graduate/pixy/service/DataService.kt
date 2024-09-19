package han.graduate.pixy.service

import han.graduate.pixy.config.stringConfigs.InternalStringConfig
import han.graduate.pixy.dto.*
import han.graduate.pixy.dto.adminOnly.AccuracyHistoryDTO
import han.graduate.pixy.dto.adminOnly.CommitInfoDTO
import han.graduate.pixy.dto.adminOnly.DockerImageInfoDTO
import han.graduate.pixy.entity.*
import han.graduate.pixy.repository.CommitRepository
import han.graduate.pixy.repository.DockerImageRepository
import han.graduate.pixy.repository.ExampleQuestionRepository
import han.graduate.pixy.repository.UsageRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate


@Service
class DataService(private val commitRepo:CommitRepository,
                  private val dockerRepo:DockerImageRepository,
                  private val usageRepo:UsageRepository,
                  private val internalStringConfig: InternalStringConfig,
                  private val exampleQuestionRepository: ExampleQuestionRepository) {

    @Transactional(readOnly = true)
    fun getCommitList(): List<CommitInfoDTO> {
        return commitRepo.findAll()
            .map{
                entity:CommitInfo -> CommitInfoDTO(
                    entity.date,
                    entity.commitId,
                    entity.valAccuracy,
                    entity.isBuilt,
                    entity.dockerName
                )
            }
    }

    @Transactional
    fun saveCommitList(commitInfoDTOList: List<CommitInfoDTO>){
        val list = commitInfoDTOList.map { dto ->
            CommitInfo(
                dto.date,
                dto.commitId,
                dto.valAccuracy,
                dto.isBuilt,
                dto.dockerName
            )
        }
        commitRepo.saveAll(list)
    }

    @Transactional(readOnly = true)
    fun getCommitAccuracyList(): AccuracyHistoryDTO {
        val entityList = commitRepo.findAll().sortedBy { it.date }
        return AccuracyHistoryDTO(
            shortCommitId = entityList.map{ it.commitId.take(internalStringConfig.data.commitIdLength.toInt()) },
            commitAccuracy = entityList.map{ it.valAccuracy ?: internalStringConfig.data.defaultCommitValAccuracy.toDouble() }
        )
    }

    @Transactional
    fun updateCommitBuilt(commitId:String,isBuilt:Boolean,dockerTagName:String){
        val temp = commitRepo.findByCommitId(commitId).orElseThrow()
        temp.changeBuiltInfo(isBuilt,dockerTagName)
    }

    @Transactional(readOnly = true)
    fun getDockerImageList():List<DockerImageInfoDTO>{
        return dockerRepo.findAll()
            .map{
                entity:DockerImageInfo->
                DockerImageInfoDTO(
                    entity.date,
                    entity.dockerName,
                    entity.isActivate
                )
            }
    }

    @Transactional
    fun saveDockerImageList(dockerImageInfoDTOList: List<DockerImageInfoDTO>){
        val list = dockerImageInfoDTOList.map { dto ->
            DockerImageInfo(
                dto.date,
                dto.dockerName
            )
        }
        dockerRepo.saveAll(list)
    }

    @Transactional(readOnly = true)
    fun getActiveDockerImageVersion(): String {
        return dockerRepo.findByIsActivateTrue().get().dockerName
    }

    @Transactional
    fun updateDockerImageStatus(targetDockerName:String){
        dockerRepo.updateActiveDocker(targetDockerName)
    }

    @Transactional(readOnly = true)
    fun getUsageList(currentPage:Int,pageSize:Int): Page<UsageInfoDTO> {
        val totalElements = usageRepo.count()
        if (totalElements == 0L) {
            return Page.empty()
        }

        val adjustedPageSize:Int = if (totalElements < pageSize) {
            totalElements.toInt()
        } else if( totalElements < currentPage * pageSize ) {
            (totalElements - currentPage * (pageSize - 1)).toInt()
        } else {
            pageSize
        }
        return usageRepo
            .findAll(PageRequest.of(currentPage,
                adjustedPageSize,
                Sort.by(Sort.Direction.DESC,"usedDateTime")
            ))
            .map{
                entity:UsageInfo -> UsageInfoDTO(
                    entity.userName,
                    entity.usedDateTime,
                    entity.gcsVoiceUri,
                    entity.isSuccess,
                    entity.answeredSentences,
                    entity.modifiedSentences
                )
            }
    }

    @Transactional(readOnly = true)
    fun getUsageResultBySessionString(sessionString: String): UsageResultDTO {
        val findOut = usageRepo.findByUserName(sessionString)
        return UsageResultDTO(findOut.answeredSentences,findOut.modifiedSentences)
    }

    @Transactional(readOnly = true)
    fun getTodayUsageCount(): Long {
        return usageRepo.countByUsedDateTime(LocalDate.now())
    }

    @Transactional
    fun saveUsage(usageInfoDTO: UsageInfoDTO){
        usageRepo.save(
            UsageInfo(
                usageInfoDTO.userName,
                usageInfoDTO.usedDateTime,
                usageInfoDTO.gcsVoiceUri,
                usageInfoDTO.isSuccess,
                usageInfoDTO.answeredSentences,
                usageInfoDTO.modifiedSentences
            ))
    }

    @Transactional(readOnly = true)
    fun getQuestionByDifficulty(difficulty: Int): String {
        return exampleQuestionRepository.getRandomQuestionByDifficulty(difficulty)
    }
}