package im.bigs.pg.external.pg

import ExceptionStatus
import org.springframework.http.HttpStatus

enum class ExternalPgExceptionStatus(
    @JvmField val status: HttpStatus,
    @JvmField val code: String,
    @JvmField val message: String,
) : ExceptionStatus {

    CARD_STOLEN_OR_LOST(HttpStatus.BAD_REQUEST, "CARD_STOLEN_OR_LOST", "도난 또는 분실된 카드입니다."),
    CARD_LIMIT_EXCEEDED(HttpStatus.PAYMENT_REQUIRED, "CARD_LIMIT_EXCEEDED", "한도가 초과되었습니다."),
    CARD_INVALID_STATUS(HttpStatus.BAD_REQUEST, "CARD_INVALID_STATUS", "정지되었거나 만료된 카드입니다."),
    CARD_TAMPERED(HttpStatus.FORBIDDEN, "CARD_TAMPERED", "유효하지 않은 카드 정보입니다."),

    PG_SYSTEM_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "PG_SYSTEM_ERROR", "외부 결제 시스템에 에러가 발생했습니다."),
    UNKNOWN_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "PG_UNKNOWN_ERROR", "알 수 없는 에러가 발생했습니다."),
    ;

    override fun getHttpStatus(): HttpStatus = status

    override fun getCode(): String = code

    override fun getMessage(): String = message
}