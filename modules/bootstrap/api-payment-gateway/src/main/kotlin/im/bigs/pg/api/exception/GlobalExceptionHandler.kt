package im.bigs.pg.api.exception

import BusinessException
import ExceptionResponse
import GlobalExceptionStatus
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    private val logger = LoggerFactory.getLogger(javaClass)

    @ExceptionHandler(BusinessException::class)
    protected fun handleBusinessException(
        exception: BusinessException,
        response: HttpServletResponse,
    ): ExceptionResponse {
        logger.error(
            "Exception ${exception.getStatus().getCode()}: ${exception.getStatus().getMessage()}"
        )

        response.status = exception.getStatus().getHttpStatus().value()
        return ExceptionResponse.from(exception.getStatus())
    }


    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException::class)
    protected fun handleMethodArgumentNotValidException(
        exception: MethodArgumentNotValidException,
    ): ExceptionResponse {
        val message =
            if (exception.bindingResult.fieldErrors.isNotEmpty()) {
                exception.bindingResult.fieldErrors.joinToString(", ") { "${it.field}: ${it.defaultMessage}" }
            } else {
                GlobalExceptionStatus.BAD_REQUEST.message
            }

        return ExceptionResponse.from(HttpStatus.BAD_REQUEST.name, message)
    }

    @ExceptionHandler(Exception::class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    protected fun handleException(exception: Exception
    ): ExceptionResponse {
        logger.error ("Exception: ${exception.javaClass.simpleName} - ${exception.message}")
        return ExceptionResponse.from(GlobalExceptionStatus.SERVER_ERROR)
    }

}
