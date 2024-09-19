package han.graduate.pixy.service

import han.graduate.pixy.config.stringConfigs.ExternalApiConfig
import han.graduate.pixy.exception.ErrorCode
import han.graduate.pixy.exception.PixyException
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Mono

@Service
class JenkinsService(private val externalApiConfig: ExternalApiConfig) {
    private val webClient: WebClient = WebClient.builder()
        .baseUrl(externalApiConfig.jenkins.baseUrl)
        .build()

    fun getJenkinsServerStatus(): Boolean {
        return this.webClient.get()
                .headers {
                    it.setBasicAuth(externalApiConfig.jenkins.username, externalApiConfig.jenkins.password)
                }
                .retrieve()
                .toBodilessEntity()
                .map { response -> response.statusCode.is2xxSuccessful }
                .onErrorReturn(false)
                .block() ?: false
    }

    fun buildDockerImage(commitId: String?, tag: String): Mono<String> {
        if (commitId.isNullOrBlank() || tag.contains(Regex("\\s"))) {
            throw PixyException(ErrorCode.JENKINS_BAD_REQUEST)
        }

        return try {
            webClient.post()
                .uri(externalApiConfig.jenkins.buildImage.path)
                .headers{
                    it.setBasicAuth(externalApiConfig.jenkins.username,
                        externalApiConfig.jenkins.password)
                }
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue("${externalApiConfig.jenkins.buildImage.requestBody[0]}=$commitId&${externalApiConfig.jenkins.buildImage.requestBody[1]}=$tag")
                .retrieve()
                .bodyToMono(String::class.java)
        } catch (e:WebClientResponseException.BadRequest) {
            throw PixyException(ErrorCode.JENKINS_BAD_REQUEST)
        } catch (ex: WebClientResponseException.Forbidden) {
            throw PixyException(ErrorCode.JENKINS_AUTH_ERROR)
        } catch (e: Error) {
            throw PixyException(ErrorCode.JENKINS_RESULT_ERROR)
        }
    }

    fun updateAPI(version: String): Mono<String> {
        return try {
            webClient.post()
                .uri(externalApiConfig.jenkins.updateAPI.path)
                .headers{
                    it.setBasicAuth(externalApiConfig.jenkins.username,
                        externalApiConfig.jenkins.password)
                }
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue("${externalApiConfig.jenkins.updateAPI.requestBody[0]}=$version")
                .retrieve()
                .bodyToMono(String::class.java)
        } catch (e:WebClientResponseException.BadRequest) {
            throw PixyException(ErrorCode.JENKINS_BAD_REQUEST)
        } catch (ex: WebClientResponseException.Forbidden) {
            throw PixyException(ErrorCode.JENKINS_AUTH_ERROR)
        } catch (e: Error) {
            throw PixyException(ErrorCode.JENKINS_RESULT_ERROR)
        }
    }
}