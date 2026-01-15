class BusinessException(
    private val status: ExceptionStatus
) : RuntimeException(status.getMessage()){
    fun getStatus() : ExceptionStatus = status
}