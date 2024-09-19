package han.graduate.pixy.service

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import han.graduate.pixy.config.stringConfigs.ExternalApiConfig
import han.graduate.pixy.dto.adminOnly.CurrentAccuracyDTO
import han.graduate.pixy.dto.internal.CommitDTO
import han.graduate.pixy.dto.internal.DockerDTO
import han.graduate.pixy.exception.ErrorCode
import han.graduate.pixy.exception.PixyException
import org.springframework.boot.json.JsonParseException
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Mono
import java.nio.charset.StandardCharsets
import java.time.ZonedDateTime
import java.util.*

@Service
class ApiCallService(private val webClientBuilder:WebClient.Builder,
                     private val gson:Gson,
                     private val externalApiConfig: ExternalApiConfig) {
    private val githubWebClient:WebClient = webClientBuilder.baseUrl(externalApiConfig.github.baseUrl).build()
    private val dockerhubWebClient:WebClient = webClientBuilder.baseUrl(externalApiConfig.dockerhub.baseUrl).build()
    private val pixyAIWebClient:WebClient = webClientBuilder.baseUrl(externalApiConfig.pixyAI.baseUrl).build()

    fun getCommitListByGithubRepository(): MutableList<CommitDTO> {
        val result: MutableList<CommitDTO> = mutableListOf()
        val responseEntity = try {
            this.githubWebClient.get()
                .uri { uriBuilder ->
                    uriBuilder.path(externalApiConfig.github.commitList.path)
                        .queryParam(externalApiConfig.github.commitList.paramName, externalApiConfig.github.commitList.paramValue)
                        .build()
                }
                .headers { headers -> headers.setBearerAuth(externalApiConfig.github.secretKey) }
                .retrieve()
                .toEntity(String::class.java)
                .block() ?: throw PixyException(ErrorCode.API_CLIENT_CREATE_FAIL)
        } catch (e: WebClientResponseException.BadRequest) {
            throw PixyException(ErrorCode.GITHUB_BAD_REQUEST)
        } catch (e: WebClientResponseException.Forbidden) {
            throw PixyException(ErrorCode.GITHUB_AUTH_ERROR)
        } catch (e: WebClientResponseException.NotFound) {
            throw PixyException(ErrorCode.GITHUB_RESULT_ERROR)
        } catch (e: Exception) {
            throw PixyException(ErrorCode.API_CLIENT_CREATE_FAIL)
        }

        val response = responseEntity.body

        if (response.isNullOrBlank()) {
            throw PixyException(ErrorCode.GITHUB_RESULT_ERROR)
        }

        try {
            val commits = gson.fromJson(response, JsonArray::class.java)
            for (commit in commits) {
                val commitObject = commit.asJsonObject
                val commitId = commitObject[externalApiConfig.github.commitList.paramName]?.asString
                    ?: throw PixyException(ErrorCode.GITHUB_RESULT_MISSMATCH)
                val dateString = commitObject[externalApiConfig.github.commitList.commit]
                    .asJsonObject[externalApiConfig.github.commitList.committer]
                    .asJsonObject[externalApiConfig.github.commitList.date]
                    .asString

                val date = ZonedDateTime.parse(dateString).toLocalDateTime()
                result.add(CommitDTO(date, commitId))
            }
        } catch (e: JsonParseException) {
            throw PixyException(ErrorCode.GITHUB_RESULT_MISSMATCH)
        }

        return result
    }

    fun getAccuracyByGithubRepository(commitId:String): Double? {
        return try {
            this.githubWebClient.get()
                .uri { uriBuilder -> uriBuilder
                    .path(externalApiConfig.github.accuracy.path)
                    .queryParam(externalApiConfig.github.accuracy.paramName, commitId)
                    .build()
                }
                .headers { headers -> headers.setBearerAuth(externalApiConfig.github.secretKey) }
                .retrieve()
                .bodyToMono(String::class.java)
                .map{ jsonResponse ->
                    try{
                        val jsonResponse = gson.fromJson(jsonResponse, JsonObject::class.java)
                        val encodedContent: String = jsonResponse
                            .get(externalApiConfig.github.accuracy.responseContent)
                            .asString
                            .replace("\n", "")
                            .trim()

                        val decodedBytes: ByteArray = Base64.getDecoder().decode(encodedContent)
                        val decodedContent = String(decodedBytes, StandardCharsets.UTF_8)

                        val lines = decodedContent.lines()
                        val lastLine = if (lines.size >= 3) lines[lines.size - 2] else null
                        lastLine?.split(",")?.get(1)?.trim()?.toDouble()
                    } catch (e: Exception) {
                        throw PixyException(ErrorCode.GITHUB_RESULT_MISSMATCH)
                    }
                }
                .block()
        } catch (e: WebClientResponseException.BadRequest) {
            throw PixyException(ErrorCode.GITHUB_BAD_REQUEST)
        } catch (e: WebClientResponseException.Forbidden) {
            throw PixyException(ErrorCode.GITHUB_AUTH_ERROR)
        } catch (e: WebClientResponseException.NotFound) {
            throw PixyException(ErrorCode.GITHUB_RESULT_ERROR)
        } catch (e: Exception) {
            throw PixyException(ErrorCode.API_CLIENT_CREATE_FAIL)
        }
    }

    fun getAiApiDockerImageList(): MutableList<DockerDTO> {
        var result: MutableList<DockerDTO> = mutableListOf()
        val response:String? = try {
            this.dockerhubWebClient.get()
                .uri(externalApiConfig.dockerhub.imageList.path)
                .retrieve()
                .bodyToMono(String::class.java)
                .block() ?: throw PixyException(ErrorCode.API_CLIENT_CREATE_FAIL)
        } catch(e: Exception) {
            throw PixyException(ErrorCode.DOCKERHUB_BAD_REQUEST)
        }

        try {
            val searchResult = gson.fromJson(response,JsonObject::class.java)
            val images = searchResult[externalApiConfig.dockerhub.imageList.resultCheckString].asJsonArray
            for (image in images){
                val imageObject = image.asJsonObject
                val dateString = imageObject[externalApiConfig.dockerhub.imageList.resultDate].asString
                val tag = imageObject[externalApiConfig.dockerhub.imageList.resultTag].asString

                val date = ZonedDateTime.parse(dateString).toLocalDateTime()
                result.add(DockerDTO(date,tag))
            }
        } catch(e: Exception) {
            throw PixyException(ErrorCode.DOCKERHUB_RESULT_MISSMATCH)
        }

        if(result.isEmpty()){
            throw PixyException(ErrorCode.DOCKERHUB_RESULT_ERROR)
        }

        return result
    }

    fun getAIServerStatus(): Boolean {
        val headersAndBody = try {
            val headers = mapOf(externalApiConfig.pixyAI.headerName to externalApiConfig.pixyAI.headerValue)
            val body = mapOf(externalApiConfig.pixyAI.fillSpeechPausesPath.requestProperty to externalApiConfig.pixyAI.fillSpeechPausesPath.requestTestString)
            Pair(headers, body)
        } catch (e: Exception) {
            throw PixyException(ErrorCode.PIXY_AI_BAD_REQUEST_FORMAT)
        }

        return this.pixyAIWebClient.post()
            .uri(externalApiConfig.pixyAI.fillSpeechPausesPath.path)
            .headers { it.setAll(headersAndBody.first) }
            .bodyValue(headersAndBody.second)
            .retrieve()
            .toBodilessEntity()
            .map{response -> response.statusCode.is2xxSuccessful }
            .onErrorReturn(false)
            .block()?:false
    }

    fun getFilledAnswer(feedbackTarget: String?): String {
        if(feedbackTarget.isNullOrEmpty()){
            throw PixyException(ErrorCode.PIXY_AI_UNFILLED_STRING_IS_EMPTY)
        }

        val headersAndBody = try {
            val headers = mapOf(externalApiConfig.pixyAI.headerName to externalApiConfig.pixyAI.headerValue)
            val body = mapOf(externalApiConfig.pixyAI.fillSpeechPausesPath.requestProperty to feedbackTarget)
            Pair(headers, body)
        } catch (e: Exception) {
            throw PixyException(ErrorCode.PIXY_AI_BAD_REQUEST_FORMAT)
        }

        val result = this.pixyAIWebClient.post()
            .uri(externalApiConfig.pixyAI.fillSpeechPausesPath.path)
            .headers { it.setAll(headersAndBody.first) }
            .bodyValue(headersAndBody.second)
            .retrieve()
            .bodyToMono(String::class.java)
            .block()

        val jsonObject: JsonObject = JsonParser.parseString(result).asJsonObject
        val combinSentence: String? = jsonObject.get(externalApiConfig.pixyAI.fillSpeechPausesPath.resultString)?.asString

        if(combinSentence.isNullOrEmpty()){
            throw PixyException(ErrorCode.PIXY_AI_RESULT_ERROR)
        }

        return combinSentence
    }

    fun getCurrentAccuracy(): CurrentAccuracyDTO {
        val path = externalApiConfig.pixyAI.currentAccuracy.path
        if (path.isNullOrEmpty()) {
            throw PixyException(ErrorCode.PIXY_AI_BAD_REQUEST_FORMAT)
        }

        val response = try {
            this.pixyAIWebClient.get()
                .uri { uriBuilder -> uriBuilder.path(path).build() }
                .retrieve()
                .bodyToMono(Map::class.java)
                .block() as? Map<String, List<String>>
        } catch (e: Exception) {
            throw PixyException(ErrorCode.API_CLIENT_CREATE_FAIL)
        }

        val (accuracies, valAccuracies) = response?.let {
            val acc = it[externalApiConfig.pixyAI.currentAccuracy.resultAccuracyProperty]?.map { it.toDouble() }
            val valAcc = it[externalApiConfig.pixyAI.currentAccuracy.resultValAccuracyProperty]?.map { it.toDouble() }

            if (acc.isNullOrEmpty() || valAcc.isNullOrEmpty()) {
                throw PixyException(ErrorCode.PIXY_AI_RESULT_ERROR)
            }

            acc to valAcc
        } ?: throw PixyException(ErrorCode.PIXY_AI_RESULT_ERROR)

        return CurrentAccuracyDTO(accuracies, valAccuracies)
    }
}