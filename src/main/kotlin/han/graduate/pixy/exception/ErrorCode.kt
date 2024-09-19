package han.graduate.pixy.exception

enum class ErrorCode(private val status:Int,
                     private val message:String) {

    GITHUB_BAD_REQUEST(400,"깃허브 API 요청 서식에 문제가 발생했습니다."),
    GITHUB_AUTH_ERROR(403,"깃허브 API 인증에 실패했습니다."),
    GITHUB_RESULT_MISSMATCH(404,"깃허브 API 요청 결과 서식이 일치하지 않습니다."),
    GITHUB_RESULT_ERROR(404,"깃허브 API 요청 결과가 존재하지 않습니다."),

    DOCKERHUB_BAD_REQUEST(400,"도커허브 API 요청 서식에 문제가 발생했습니다."),
    DOCKERHUB_RESULT_MISSMATCH(404,"도커허브 API 요청 결과 서식이 일치하지 않습니다."),
    DOCKERHUB_RESULT_ERROR(404,"도커허브 API 요청 결과가 존재하지 않습니다."),

    JENKINS_BAD_REQUEST(400,"젠킨스 API 요청 서식에 문제가 발생했습니다."),
    JENKINS_AUTH_ERROR(403,"젠킨스 API 인증에 실패했습니다."),
    JENKINS_RESULT_ERROR(500,"젠킨스 API 요청에 실패했습니다."),

    AUDIO_IS_EMPTY(400,"음성 파일이 존재하지 않습니다."),
    UNNAMED_AUDIO(400,"명명되지 않은 음성 파일입니다."),
    AUDIO_FORMAT_MISSMATCH(400,"지원하지 않는 음원 포맷입니다."),
    BAD_AUDIO_STORAGE_URI(400,"음성 저장소 URI가 유효하지 않습니다."),
    AUDIO_INFORMATION_MISSMATCH(400,"음원 정보가 일치하지 않습니다."),
    AUDIO_ANALYZE_ABNORMAL_TERMINATION(404,"음성 분석 중 오류가 발생했습니다."),
    SPEACH_TO_TEXT_FAILED(404,"음성 텍스트화 작업 결과에 오류가 발생했습니다."),
    API_CLIENT_CREATE_FAIL(500,"외부 API 클라이언트 호출에 실패했습니다. 관리자에게 문의 바랍니다."),

    PIXY_AI_UNFILLED_STRING_IS_EMPTY(400,"필러를 삽입할 문자열이 주어지지 않았습니다."),
    PIXY_AI_RESULT_ERROR(404,"Pixy API 호출 결과에 오류가 발생했습니다."),
    PIXY_AI_BAD_REQUEST_FORMAT(500,"서버 요청 형식에 문제가 발생했습니다. 관리자에게 문의 바랍니다."),

    ORTHOGRAPHY_STRING_IS_EMPTY(400,"맞춤법을 검사할 문자열이 주어지지 않았습니다."),
    ORTHOGRAPHY_CHECK_ERROR(404,"맞춤법 검사 중 오류가 발생했습니다."),
    ORTHOGRAPHY_RESULT_ERROR(404,"맞춤법 검사 결과에 오류가 발생했습니다."),
    LANGUAGE_CODE_MISSMATCH(500,"잘못된 언어 코드가 사용되고 있습니다. 관리자에게 문의 바랍니다."),

    INTERNAL_SERVER_ERROR(500, "Pixy 서버에 문제가 발생했습니다. 관리자에게 문의 바랍니다.");

    fun getStatus():Int { return status }
    fun getMessage():String { return message }
}