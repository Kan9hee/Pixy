package han.graduate.pixy.exception

import han.graduate.pixy.dto.ErrorResponseDTO
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(PixyException::class)
    fun customException(e: PixyException): ResponseEntity<ErrorResponseDTO> {
        val errorResponse = ErrorResponseDTO(
            e.getErrorCode().getStatus(),
            e.getErrorCode().getMessage()
        )

        return ResponseEntity
            .status(e.getErrorCode().getStatus())
            .body(errorResponse)
    }
}