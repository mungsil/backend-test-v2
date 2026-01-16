package im.bigs.pg.application.payment.service

import im.bigs.pg.application.partner.port.out.FeePolicyOutPort
import im.bigs.pg.application.partner.port.out.PartnerOutPort
import im.bigs.pg.application.payment.port.`in`.PaymentCommand
import im.bigs.pg.application.payment.port.out.PaymentOutPort
import im.bigs.pg.application.pg.port.out.PgApproveRequest
import im.bigs.pg.application.pg.port.out.PgApproveResult
import im.bigs.pg.application.pg.port.out.PgClientOutPort
import im.bigs.pg.domain.partner.FeePolicy
import im.bigs.pg.domain.partner.Partner
import im.bigs.pg.domain.payment.Payment
import im.bigs.pg.domain.payment.PaymentStatus
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals

class 결제서비스Test {
    private val partnerRepo = mockk<PartnerOutPort>()
    private val feeRepo = mockk<FeePolicyOutPort>()
    private val paymentRepo = mockk<PaymentOutPort>()
    private val pgClient = object : PgClientOutPort {
        override fun supports(partnerId: Long) = true
        override fun approve(request: PgApproveRequest) =
            PgApproveResult("APPROVAL-123", LocalDateTime.of(2024,1,1,0,0), PaymentStatus.APPROVED)
    }

    @Test
    @DisplayName("결제 시 수수료 정책을 적용하고 저장해야 한다")
    fun `결제 시 수수료 정책을 적용하고 저장해야 한다`() {
        val service = PaymentService(partnerRepo, feeRepo, paymentRepo, listOf(pgClient))
        givenPartner(id = 1L, code = "TEST")
        givenFeePolicy(
            partnerId = 1L,
            percentage = BigDecimal("0.0300"),
            fixedFee = BigDecimal("100")
        )
        val savedSlot = slot<Payment>()
        every { paymentRepo.save(capture(savedSlot)) } answers { savedSlot.captured.copy(id = 99L) }

        val cmd = PaymentCommand.of(
            partnerId = 1L,
            amount = BigDecimal(10000),
            password = "12",
            cardNumber = "1212-1212-1212-1212",
            expiryDate = "1228",
            birthDate = "19990112",
            productName = "테스트"
        )
        val res = service.pay(cmd)

        assertEquals(99L, res.id)
        assertEquals(BigDecimal("400"), res.feeAmount)
        assertEquals(BigDecimal("9600"), res.netAmount)
        assertEquals(PaymentStatus.APPROVED, res.status)
    }

    @Test
    @DisplayName("제휴사 정책에 따라 서로 다른 수수료가 적용된다")
    fun `제휴사 정책에 따라 서로 다른 수수료가 적용된다`() {
        // given
        val service = PaymentService(partnerRepo, feeRepo, paymentRepo, listOf(pgClient))

        givenPartner(1L, "TEST_A")
        givenPartner(2L, "TEST_B")

        givenFeePolicy(
            partnerId = 1L,
            percentage = BigDecimal("0.0300"),
            fixedFee = BigDecimal("100")
        )
        givenFeePolicy(
            partnerId = 2L,
            percentage = BigDecimal("0.0600"),
            fixedFee = BigDecimal("200")
        )

        val slot = slot<Payment>()
        every { paymentRepo.save(capture(slot)) } answers { slot.captured.copy(id = 100L) }

        // when
        val resultA = service.pay(
            PaymentCommand.of(
                partnerId = 1L,
                amount = BigDecimal(10000),
                password = "12",
                cardNumber = "1212-1212-1212-1212",
                expiryDate = "1228",
                birthDate = "19990112",
                productName = "테스트"
            )
        )

        val resultB = service.pay(
            PaymentCommand.of(
                partnerId = 2L,
                amount = BigDecimal(10000),
                password = "12",
                cardNumber = "1212-1212-1212-1212",
                expiryDate = "1228",
                birthDate = "19990112",
                productName = "테스트"
            )
        )

        // then
        assertThat(resultA.appliedFeeRate).isEqualTo("0.0300")
        assertThat(resultB.appliedFeeRate).isEqualTo("0.0600")

        assertThat(resultA.feeAmount).isEqualTo(BigDecimal("400")) // 10000 * 3% + 100
        assertThat(resultB.feeAmount).isEqualTo(BigDecimal("800")) // 10000 * 6% + 200

        assertThat(resultA.feeAmount).isNotEqualTo(resultB.feeAmount)
    }

    private fun givenPartner(id: Long, code: String) {
        every { partnerRepo.findById(id) } returns
                Partner(id, code, code, true)
    }

    private fun givenFeePolicy(
        partnerId: Long,
        percentage: BigDecimal,
        fixedFee: BigDecimal
    ) {
        every { feeRepo.findEffectivePolicy(partnerId, any()) } returns
                FeePolicy(
                    id = Random.nextLong(),
                    partnerId = partnerId,
                    effectiveFrom = LocalDateTime.of(2020, 1, 1, 0, 0),
                    percentage = percentage,
                    fixedFee = fixedFee
                )
    }

}
