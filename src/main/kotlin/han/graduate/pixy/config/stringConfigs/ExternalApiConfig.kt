package han.graduate.pixy.config.stringConfigs

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "external-api")
class ExternalApiConfig {
    lateinit var grafana:String
    lateinit var github: GithubProperties
    lateinit var dockerhub: DockerhubProperties
    lateinit var jenkins: JenkinsProperties
    lateinit var pixyAI: PixyApiProperties

    class GithubProperties{
        lateinit var baseUrl: String
        lateinit var secretKey: String
        lateinit var commitList: CommitListProperties
        lateinit var accuracy: AccuracyProperties

        class CommitListProperties{
            lateinit var path: String
            lateinit var paramName: String
            lateinit var paramValue: String
            lateinit var commit: String
            lateinit var committer: String
            lateinit var date: String
        }

        class AccuracyProperties{
            lateinit var path: String
            lateinit var paramName: String
            lateinit var responseContent: String
            lateinit var resultContent: String
        }
    }

    class DockerhubProperties{
        lateinit var baseUrl: String
        lateinit var imageList: ImageListProperties

        class ImageListProperties{
            lateinit var path: String
            lateinit var resultCheckString: String
            lateinit var resultDate: String
            lateinit var resultTag: String
        }
    }

    class JenkinsProperties{
        lateinit var baseUrl: String
        lateinit var username: String
        lateinit var password: String
        lateinit var buildImage: BuildImageProperties
        lateinit var updateAPI: UpdateApiProperties

        class BuildImageProperties{
            lateinit var path: String
            lateinit var requestBody: List<String>
        }

        class UpdateApiProperties{
            lateinit var path: String
            lateinit var requestBody: List<String>
        }
    }

    class PixyApiProperties{
        lateinit var baseUrl: String
        lateinit var headerName: String
        lateinit var headerValue: String
        lateinit var fillSpeechPausesPath: FillerFuncProperties
        lateinit var currentAccuracy: AccuracyFuncProperties

        class FillerFuncProperties{
            lateinit var path: String
            lateinit var requestProperty: String
            lateinit var requestTestString: String
            lateinit var resultString: String
        }

        class AccuracyFuncProperties{
            lateinit var path: String
            lateinit var resultAccuracyProperty: String
            lateinit var resultValAccuracyProperty: String
        }
    }
}