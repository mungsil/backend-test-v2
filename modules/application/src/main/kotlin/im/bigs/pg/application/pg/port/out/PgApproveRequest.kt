package im.bigs.pg.application.pg.port.out

import im.bigs.pg.domain.payment.CardNumber
import java.math.BigDecimal

/** PG 승인 요청 최소 정보. */
data class PgApproveRequest(
    val amount: BigDecimal,
    val password: String,
    val cardNumber: CardNumber,
    val expiry: String,
    val birthDate: String,
)
