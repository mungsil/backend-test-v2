class InternalServerException(
) : RuntimeException(GlobalExceptionStatus.SERVER_ERROR.message){
}