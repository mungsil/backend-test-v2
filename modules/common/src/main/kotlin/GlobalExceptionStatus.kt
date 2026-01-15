import org.springframework.http.HttpStatus

enum class GlobalExceptionStatus(
    @JvmField val status: HttpStatus,
    @JvmField val code: String,
    @JvmField val message: String,
) : ExceptionStatus {

    BAD_REQUEST(HttpStatus.BAD_REQUEST, "GLOBAL_BAD_REQUEST", "잘못된 요청입니다."),
    SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "GLOBAL_INTERNAL_SERVER_ERROR", "예상치 못한 서버 에러가 발생했습니다"),
    ;
    override fun getHttpStatus(): HttpStatus = status

    override fun getCode(): String = code

    override fun getMessage(): String = message
}