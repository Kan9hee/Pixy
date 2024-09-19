package han.graduate.pixy.controller

import han.graduate.pixy.config.stringConfigs.ExternalApiConfig
import han.graduate.pixy.dto.*
import han.graduate.pixy.dto.adminOnly.*
import han.graduate.pixy.service.ApiCallService
import han.graduate.pixy.service.CompositeService
import han.graduate.pixy.service.DataService
import han.graduate.pixy.service.JenkinsService
import org.springframework.data.domain.Page
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("\${path.admin.apiMapping.root}")
class AdminController(private val apiCallService: ApiCallService,
                      private val dataService: DataService,
                      private val jenkinsService: JenkinsService,
                      private val compositeService: CompositeService,
                      private val externalApiConfig: ExternalApiConfig) {

    @GetMapping("\${path.admin.apiMapping.accuracyHistory}")
    fun getAccuracyHistory(): AccuracyHistoryDTO {
        return dataService.getCommitAccuracyList()
    }

    @GetMapping("\${path.admin.apiMapping.monitoringUrl}")
    fun getMonitoringUrl(): String {
        return externalApiConfig.grafana
    }

    @GetMapping("\${path.admin.apiMapping.currentAccuracy}")
    fun getCurrentAccuracy(): CurrentAccuracyDTO {
        return apiCallService.getCurrentAccuracy()
    }

    @GetMapping("\${path.admin.apiMapping.todayUsage}")
    fun getTodayUsage(): Long {
        return dataService.getTodayUsageCount()
    }

    @GetMapping("\${path.admin.apiMapping.jenkinsServerStatus}")
    fun getJenkinsServerStatus(): Boolean {
        return jenkinsService.getJenkinsServerStatus()
    }

    @GetMapping("\${path.admin.apiMapping.apiServerStatus}")
    fun getApiServerStatus(): Boolean {
        return apiCallService.getAIServerStatus()
    }

    @GetMapping("\${path.admin.apiMapping.apiVersion}")
    fun getApiVersion(): String {
        return dataService.getActiveDockerImageVersion()
    }

    @GetMapping("\${path.admin.apiMapping.commitInfoList}")
    fun commitInfoList(): List<CommitInfoDTO> {
        return dataService.getCommitList()
    }

    @GetMapping("\${path.admin.apiMapping.dockerImageList}")
    fun dockerImageList(): List<DockerImageInfoDTO> {
        return dataService.getDockerImageList()
    }

    @GetMapping("\${path.admin.apiMapping.usageList}")
    fun usageList(@RequestParam("currentPage", required = true) currentPage: Int,
                  @RequestParam("pageSize", required = true) pageSize: Int): Page<UsageInfoDTO> {
        return dataService.getUsageList(currentPage,pageSize)
    }

    @PostMapping("\${path.admin.apiMapping.refreshCommitInfoList}")
    fun refreshCommitInfoList() {
        compositeService.refreshCommitInfo()
    }

    @PostMapping("\${path.admin.apiMapping.refreshDockerImageList}")
    fun refreshDockerImageList() {
        compositeService.refreshDockerImageInfo()
    }

    @PostMapping("\${path.admin.apiMapping.buildCommit}")
    fun buildCommit(@RequestBody jenkinsOrderDTO: JenkinsOrderDTO) {
        jenkinsService.buildDockerImage(jenkinsOrderDTO.commitId,jenkinsOrderDTO.tag)
    }

    @PostMapping("\${path.admin.apiMapping.updateApi}")
    fun updateApi(@RequestBody jenkinsOrderDTO: JenkinsOrderDTO) {
        jenkinsService.updateAPI(jenkinsOrderDTO.tag)
    }

    @PostMapping("\${path.admin.apiMapping.jenkinsWebhook}")
    fun handleJenkinsWebhook(@RequestBody jobInfoDTO: JobInfoDTO) {
        if(jobInfoDTO.isSuccess) {
            if(jobInfoDTO.jobName == "buildCommit")
                dataService.updateCommitBuilt(jobInfoDTO.params[0],true,jobInfoDTO.params[1])
            else if (jobInfoDTO.jobName == "updateApi")
                dataService.updateDockerImageStatus(jobInfoDTO.params[0])
        }
    }
}