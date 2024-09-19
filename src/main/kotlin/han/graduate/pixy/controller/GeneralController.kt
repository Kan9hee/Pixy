package han.graduate.pixy.controller

import han.graduate.pixy.dto.JwtTokenDTO
import han.graduate.pixy.dto.LoginDTO
import han.graduate.pixy.dto.UsageResultDTO
import han.graduate.pixy.service.CustomUserDetailsService
import han.graduate.pixy.service.DataService
import han.graduate.pixy.service.AudioService
import han.graduate.pixy.service.CompositeService
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.util.*

@RestController
@RequestMapping("\${path.open.apiMapping.root}")
class GeneralController(private val dataService: DataService,
                        private val customUserDetailsService: CustomUserDetailsService,
                        private val compositeService: CompositeService) {

    @GetMapping("\${path.open.apiMapping.generateSession}")
    fun generateSession():String {
        return UUID.randomUUID().toString()
    }

    @GetMapping("\${path.open.apiMapping.usageResult}")
    fun getUsageResult(@RequestParam(required = false) sessionId: String): UsageResultDTO {
        return dataService.getUsageResultBySessionString(sessionId)
    }

    @PostMapping("\${path.open.apiMapping.uploadWav}", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun uploadWav(@RequestParam("voice") voice: MultipartFile, @RequestParam("sessionId") sessionId: String) {
        compositeService.uploadAndAnalyzeAudio(voice,sessionId)
    }

    @PostMapping("\${path.open.apiMapping.login}")
    fun login(@RequestBody loginDTO: LoginDTO, response: HttpServletResponse): JwtTokenDTO {
        val jwtTokenDTO = customUserDetailsService.logIn(loginDTO.userName,loginDTO.pw)
        response.addCookie(
            Cookie("refreshToken", jwtTokenDTO.refreshToken).apply {
                isHttpOnly = true
                secure = true
                path = "/"
                maxAge = 60 * 60 * 24 * 7
        })

        return jwtTokenDTO
    }

    @PostMapping("\${path.open.apiMapping.refresh}")
    fun refreshToken(request: HttpServletRequest, response: HttpServletResponse): JwtTokenDTO {
        return customUserDetailsService.refreshAccessToken(request)
    }

    @GetMapping("\${path.open.apiMapping.exampleQuestion}")
    fun getQuestion(@RequestParam("difficulty") difficulty: Int): String {
        return dataService.getQuestionByDifficulty(difficulty)
    }
}