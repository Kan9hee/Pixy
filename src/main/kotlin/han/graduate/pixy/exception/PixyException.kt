package han.graduate.pixy.exception

class PixyException(private val errorCode: ErrorCode)
    :RuntimeException(errorCode.getMessage()) {
    fun getErrorCode(): ErrorCode { return errorCode }
}