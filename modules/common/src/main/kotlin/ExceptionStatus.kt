import org.springframework.http.HttpStatusCode

interface ExceptionStatus {

    fun getCode(): String
    fun getMessage(): String
    fun getHttpStatus(): HttpStatusCode

}