package han.graduate.pixy.config.stringConfigs

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "path")
class ControllerPathConfig {
    lateinit var admin: AdminProperties
    lateinit var open: OpenProperties

    class AdminProperties {
        lateinit var viewMapping: AdminViewProperties
        lateinit var pageFile: AdminPageProperties
        lateinit var apiMapping: AdminApiProperties

        class AdminViewProperties {
            lateinit var root: String
            lateinit var commitManage: String
            lateinit var dockerManage: String
            lateinit var usageStatus: String
        }

        class AdminPageProperties {
            lateinit var root: String
            lateinit var commitManage: String
            lateinit var dockerManage: String
            lateinit var usageStatus: String
        }

        class AdminApiProperties {
            lateinit var root: String
            lateinit var monitoringUrl: String
            lateinit var accuracyHistory: String
            lateinit var currentAccuracy: String
            lateinit var todayUsage: String
            lateinit var jenkinsServerStatus: String
            lateinit var apiServerStatus: String
            lateinit var apiVersion: String
            lateinit var commitInfoList: String
            lateinit var dockerImageList: String
            lateinit var usageList: String
            lateinit var refreshCommitInfoList: String
            lateinit var refreshDockerImageList: String
            lateinit var buildCommit: String
            lateinit var updateApi: String
            lateinit var jenkinsWebhook: String
        }
    }

    class OpenProperties {
        lateinit var viewMapping: ViewProperties
        lateinit var pageFile: PageProperties
        lateinit var apiMapping: ApiProperties

        class ViewProperties {
            lateinit var main: String
            lateinit var login: String
            lateinit var recordPage: String
        }

        class PageProperties {
            lateinit var main: String
            lateinit var login: String
            lateinit var recordPage: String
        }

        class ApiProperties {
            lateinit var root: String
            lateinit var generateSession: String
            lateinit var usageResult: String
            lateinit var uploadWav: String
            lateinit var login: String
            lateinit var logout: String
            lateinit var refresh: String
            lateinit var exampleQuestion:String
        }
    }
}