package im.bigs.pg.application.payment.port.`in`

import java.math.BigDecimal
import java.time.LocalDate

/**
 * 결제 생성에 필요한 최소 입력.
 *
 * @property partnerId 제휴사 식별자
 * @property amount 결제 금액(정수 금액 권장)
 * @property password 카드 비밀번호
 * @property cardNumber 전체 카드 번호 16자리
 * @property expiryDate 카드 만료기한
 * @property birthDate 생년월일
 * @property productName 상품이름(없을 수 있음)
 */
data class PaymentCommand(
    val partnerId: Long,
    val amount: BigDecimal,
    val password: String,
    val cardNumber: String,
    val expiryDate: String,
    val birthDate: String,
    val productName: String? = null,
)
