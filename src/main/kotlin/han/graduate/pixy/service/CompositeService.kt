package han.graduate.pixy.service

import han.graduate.pixy.config.stringConfigs.InternalStringConfig
import han.graduate.pixy.dto.LoadingProgressDTO
import han.graduate.pixy.dto.UsageInfoDTO
import han.graduate.pixy.dto.adminOnly.CommitInfoDTO
import han.graduate.pixy.dto.adminOnly.DockerImageInfoDTO
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDateTime

/**
 * 복합 동작 처리 서비스 클래스.
 * 여러 서비스 클래스의 메서드를 조합하여
 * 커밋 정보 업데이트와 음성 분석 등의 작업을 수행함.
 */
@Service
class CompositeService(private val apiCallService: ApiCallService,
                       private val dataService: DataService,
                       private val audioService: AudioService,
                       private val checkService: LanguageCheckService,
                       private val messagingTemplate: SimpMessagingTemplate,
                       private val internalStringConfig: InternalStringConfig) {

    /**
     * 커밋 정보 업데이트 메서드.
     * 깃허브 api를 호출하여 리포지토리의 커밋 리스트를 가져오고,
     * 데이터베이스에 누락된 커밋 정보를 저장함.
     */
    fun refreshCommitInfo() {
        //깃허브 api 호출 - 리포지토리 전체 커밋 리스트
        val commitDTOList = apiCallService.getCommitListByGithubRepository()

        //데이터베이스 내 커밋 리스트에서 커밋 ID 추출
        val existingCommitIds = dataService.getCommitList().map{ it.commitId }

        //리포지토리 전체 커밋 리스트와 데이터베이스 정보 대조
        //대조 결과 데이터베이스에 존재하지 않는 데이터가 확인된 경우,
        //해당 커밋의 val_accuracy 정보를 가져와 List에 담음.
        val newCommits = commitDTOList
            .filter { it.commitId !in existingCommitIds }
            .map{ commitDTO ->
                val accuracy = apiCallService.getAccuracyByGithubRepository(commitDTO.commitId)
                CommitInfoDTO(
                    date = commitDTO.date,          //커밋 날짜
                    commitId = commitDTO.commitId,  //커밋 id (full)
                    valAccuracy = accuracy,         //커밋 AI val_accuracy
                    //빌드하지 않은 새로운 commit 정보이므로, isBuilt와 dockerName은 디폴트 값을 이용
                )
            }

        //List에 값이 존재할 경우, 해당 정보들을 데이터베이스에 저장함.
        if(newCommits.isNotEmpty())
            dataService.saveCommitList(newCommits)
    }

    /**
     * 도커 이미지 정보 업데이트 메서드.
     * 도커허브 api를 호출하여 리포지토리의 태그 리스트를 가져오고,
     * 데이터베이스에 누락된 이미지 정보를 저장함.
     */
    fun refreshDockerImageInfo(){
        //도커허브 api 호출 - 리포지토리 전체 도커 이미지 리스트
        val dockerImages = apiCallService.getAiApiDockerImageList()

        //데이터베이스 내 도커 이미지 리스트에서 이미지 태그명 추출
        val existingDockerNames = dataService.getDockerImageList().map{ it.dockerName }

        //리포지토리 전체 도커 이미지 리스트와 데이터베이스 정보 대조
        //대조 결과 데이터베이스에 존재하지 않는 데이터가 확인된 경우,
        //해당 이미지 정보를 List에 담음.
        val newDockerImages = dockerImages
            .filter { it.dockerName !in existingDockerNames }
            .map{ dockerDTO -> DockerImageInfoDTO(
                    date = dockerDTO.date,
                    dockerName = dockerDTO.dockerName
                )
            }

        //List에 값이 존재할 경우, 해당 정보들을 데이터베이스에 저장함.
        if(newDockerImages.isNotEmpty())
            dataService.saveDockerImageList(newDockerImages)
    }

    /**
     * 음성 피드백 메서드.
     * 음성 파일을 분석하여 구글 클라우드 스토리지에 업로드하고,
     * STT 변환, 필러 삽입, 문법 교정 단계를 거쳐 최종 결과를 생성함.
     * 각 단계별 작업 진행 현황을 로딩 스크린에 전달함.
     *
     * @param voice 음성 파일 (MultipartFile)
     * @param sessionId 소켓 작업 세션 식별자
     * @throws Exception 각 작업 단계에서 발생하는 오류 처리
     */
    fun uploadAndAnalyzeAudio(voice: MultipartFile, sessionId: String) {
        val progress = LoadingProgressDTO(
            internalStringConfig.loading.totalStep.toInt(),
            0,
            internalStringConfig.loading.stepTexts[0])

        var gcsUri: String? = null      //구글 클라우드 스토리지
        var sttResult: String? = null   //stt 변환 문자열
        var finalResult: String? = null //최종 피드백 결과 문자열

        try {
            //음성 분석 단계
            sendProgress(sessionId,progress)
            val voiceInfo=audioService.analyzeAudio(voice)

            //구글 클라우드 스토리지 업로드 단계
            progress.currentStep++
            progress.message = internalStringConfig.loading.stepTexts[progress.currentStep]
            sendProgress(sessionId,progress)
            gcsUri=audioService.uploadAudio(voice,sessionId)

            //구글 stt 텍스트 변환 단계
            progress.currentStep++
            progress.message = internalStringConfig.loading.stepTexts[progress.currentStep]
            sendProgress(sessionId,progress)
            sttResult = audioService.audioToTextWithFillerTag(gcsUri,voiceInfo.encoding,voiceInfo.sampleRateHertz)

            //필러 삽입 단계
            progress.currentStep++
            progress.message = internalStringConfig.loading.stepTexts[progress.currentStep]
            sendProgress(sessionId,progress)
            val filledResult = sttResult?.let { apiCallService.getFilledAnswer(it) }

            //문법 교정 단계
            progress.currentStep++
            progress.message = internalStringConfig.loading.stepTexts[progress.currentStep]
            sendProgress(sessionId,progress)
            finalResult = filledResult?.let { checkService.sentenceCorrection(it) }

            //작업 성공 정보 저장
            dataService.saveUsage(
                UsageInfoDTO(
                    sessionId,
                    LocalDateTime.now(),
                    gcsUri,
                    true,
                    sttResult,
                    finalResult
                )
            )
        } catch(e:Exception) {
            //각 단계별 오류 발생에 따른 에러 메시지 적용
            val errorMessage = e.message ?: "Unknown error"
            when (progress.currentStep) {
                //음성 분석, 업로드 실패
                0,1 -> {
                    gcsUri = errorMessage
                    sttResult = errorMessage
                    finalResult = errorMessage
                }
                //stt 변환 실패
                2 -> {
                    sttResult = errorMessage
                    finalResult = errorMessage
                }
                //필러 삽입, 문법 교정 실패
                3,4 -> {
                    finalResult = errorMessage
                }
            }

            //작업 실패 정보 저장
            dataService.saveUsage(
                UsageInfoDTO(
                    sessionId,
                    LocalDateTime.now(),
                    gcsUri,
                    false,
                    sttResult,
                    finalResult
                )
            )
        } finally {
            //최종 단계의 값은 항상 최대 단계의 값으로 고정
            //오류 발생 시 중간 단계를 건너뛰었음을 알리기 위함
            progress.currentStep=internalStringConfig.loading.totalStep.toInt()
            progress.message = internalStringConfig.loading.stepTexts[progress.currentStep]
            sendProgress(sessionId,progress)
        }
    }

    /**
     * 작업 단계 현황 전달 메서드.
     * 연결된 소켓을 통해 작업 정보를 전달함.
     *
     * @param sessionId 소켓 작업 세션 식별자
     * @param progress 현재 작업 정보
     */
    private fun sendProgress(sessionId:String, progress: LoadingProgressDTO){
        messagingTemplate.convertAndSendToUser(sessionId,internalStringConfig.loading.destination,progress)
    }
}