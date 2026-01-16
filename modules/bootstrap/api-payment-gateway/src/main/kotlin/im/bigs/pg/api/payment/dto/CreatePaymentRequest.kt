package im.bigs.pg.api.payment.dto

import jakarta.validation.constraints.Min
import java.math.BigDecimal

data class CreatePaymentRequest(
    val partnerId: Long,
    @field:Min(1)
    val amount: BigDecimal,
    val password: String,
    val cardNumber: String,
    val expiryDate: String,
    val birthDate: String,
    val productName: String?= null,
)

