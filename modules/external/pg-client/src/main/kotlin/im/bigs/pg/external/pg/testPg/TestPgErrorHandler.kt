package im.bigs.pg.external.pg.testPg

import InternalServerException
import com.fasterxml.jackson.databind.ObjectMapper
import im.bigs.pg.external.pg.ExternalPgException
import im.bigs.pg.external.pg.ExternalPgExceptionStatus
import im.bigs.pg.external.pg.testPg.TestPgClient.ErrorResponse
import org.slf4j.LoggerFactory
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.client.ClientHttpResponse
import org.springframework.stereotype.Component
import org.springframework.web.client.ResponseErrorHandler
import java.net.URI

@Component
class TestPgErrorHandler(
    private val objectMapper: ObjectMapper
) : ResponseErrorHandler {

    private val logger = LoggerFactory.getLogger(javaClass)

    override fun hasError(response: ClientHttpResponse): Boolean {
        return response.statusCode.is4xxClientError || response.statusCode.is5xxServerError
    }

    override fun handleError(url: URI, method: HttpMethod, response: ClientHttpResponse) {
        logger.error("알 수 없는 오류가 발생했습니다. 응답: $response")

        if (response.statusCode.is5xxServerError()) {
            throw ExternalPgException(ExternalPgExceptionStatus.PG_SYSTEM_ERROR)
        }

        if (response.statusCode.isSameCodeAs(HttpStatus.UNAUTHORIZED)) {
            logger.error("유효하지 않은 KEY이거나 KEY 정보가 없습니다.")
            throw InternalServerException()
        }

        val rawBody = response.body
        val errorResponse = objectMapper.readValue(rawBody, ErrorResponse::class.java)
        val status = when (errorResponse.code) {
            1001 -> ExternalPgExceptionStatus.CARD_STOLEN_OR_LOST
            1002 -> ExternalPgExceptionStatus.CARD_LIMIT_EXCEEDED
            1003 -> ExternalPgExceptionStatus.CARD_INVALID_STATUS
            1004, 1005 -> ExternalPgExceptionStatus.CARD_TAMPERED
            else -> ExternalPgExceptionStatus.UNKNOWN_ERROR
        }

        throw ExternalPgException(status)
    }
}
