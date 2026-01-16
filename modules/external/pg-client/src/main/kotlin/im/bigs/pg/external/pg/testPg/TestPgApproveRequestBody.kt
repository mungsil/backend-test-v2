package im.bigs.pg.external.pg.testPg

import im.bigs.pg.application.pg.port.out.PgApproveRequest
import java.math.BigDecimal

data class TestPgApproveRequestBody(
    val amount: BigDecimal,
    val password: String,
    val cardNumber: String,
    val expiry: String,
    val birthDate: String,
) {
    companion object {
        fun from(request: PgApproveRequest): TestPgApproveRequestBody{
            with(request) {
                require(birthDate.matches(Regex("""^\d{8}$"""))) {
                    "생년월일은 YYYYMMDD 형식이어야 합니다: $birthDate"
                }

                require(expiry.matches(Regex("""^\d{4}$"""))) {
                    "유효기간은 MMYY 형식이어야 합니다: $expiry"
                }

                require(password.matches(Regex("""^\d{2}$"""))) {
                    "비밀번호는 앞 2자리 숫자여야 합니다: $password"
                }

                require(amount >= BigDecimal.ONE) {
                    "결제 금액은 1원 이상이어야 합니다: $amount"
                }
            }

            return TestPgApproveRequestBody(
                amount = request.amount,
                password = request.password,
                cardNumber = request.cardNumber.value,
                expiry = request.expiry,
                birthDate = request.birthDate,
            )
        }
    }
}
