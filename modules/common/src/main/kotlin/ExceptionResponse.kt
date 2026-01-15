
data class ExceptionResponse(
    val errorCode: String,
    val errorMessage: String,
) {

    companion object {
        fun from(status: ExceptionStatus): ExceptionResponse {
            return ExceptionResponse(
                errorCode = status.getCode(),
                errorMessage = status.getMessage(),
            )
        }

        fun from(code: String, message: String): ExceptionResponse {
            return ExceptionResponse(
                errorCode = code,
                errorMessage = message,
            )
        }
    }

}