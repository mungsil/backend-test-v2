package im.bigs.pg.application.payment.service

import im.bigs.pg.application.payment.port.`in`.QueryFilter
import im.bigs.pg.application.payment.port.out.PaymentOutPort
import im.bigs.pg.application.payment.port.out.PaymentPage
import im.bigs.pg.application.payment.port.out.PaymentSummaryProjection
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.*

class QueryPaymentsServiceTest {

    private val paymentRepo = mockk<PaymentOutPort>()
    private val service = QueryPaymentsService(paymentRepo)

    @Test
    @DisplayName("디코딩된 커서를 통해 결제 내역을 조회한다")
    fun `디코딩된 커서를 통해 결제 내역을 조회한다`() {
        // given
        val createdAt = Instant.parse("2020-10-01T10:00:00Z")
        val id = 12345L

        val rawCursor = "${createdAt.toEpochMilli()}:$id"
        val encodedCursor = Base64.getUrlEncoder().withoutPadding().encodeToString(rawCursor.toByteArray())

        val filter = QueryFilter(partnerId = 1L, status = "APPROVE", cursor = encodedCursor)

        every { paymentRepo.findBy(any()) } returns PaymentPage(emptyList(), false, null, null)
        every { paymentRepo.summary(any()) } returns PaymentSummaryProjection(0, BigDecimal(100), BigDecimal(100))

        // when
        service.query(filter)

        // then
        verify {
            paymentRepo.findBy(withArg {
                assertThat(it.cursorCreatedAt).isEqualTo(LocalDateTime.ofInstant(createdAt, ZoneId.of("UTC")))
                assertThat(it.cursorId).isEqualTo(id)
            })
        }
    }

    @Test
    @DisplayName("다음 결제 내역이 존재하면 커서를 함께 반환한다")
    fun `다음 결제 내역이 존재하면 커서를 함께 반환한다`() {
        // given
        val filter = QueryFilter(partnerId = 1L, status = "APPROVE")
        val nextTime = LocalDateTime.of(2025, 10, 1, 1, 1)
        val nextId = 999L

        every { paymentRepo.findBy(any()) } returns PaymentPage(
            items = listOf(mockk()),
            nextCursorCreatedAt = nextTime,
            nextCursorId = nextId,
            hasNext = true
        )
        every { paymentRepo.summary(any()) } returns PaymentSummaryProjection(
            5, BigDecimal(100), BigDecimal(100)
        )

        // when
        val result = service.query(filter)

        // then
        assertThat(result.hasNext).isTrue()
        assertThat(result.nextCursor).isNotNull()
        assertThat(result.items).isNotEmpty()
        assertThat(result.summary.count).isEqualTo(5)
        assertThat(result.summary.totalAmount).isEqualTo(BigDecimal(100))
        assertThat(result.summary.totalNetAmount).isEqualTo(BigDecimal(100))

        val decoded = String(Base64.getUrlDecoder().decode(result.nextCursor))
        val expectedTimeMillis = nextTime.toInstant(ZoneOffset.UTC).toEpochMilli()
        assertThat(decoded).isEqualTo("$expectedTimeMillis:$nextId")
    }
}
