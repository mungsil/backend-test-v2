package im.bigs.pg.external.pg.testPg

import com.fasterxml.jackson.databind.ObjectMapper
import im.bigs.pg.application.pg.port.out.PgApproveRequest
import im.bigs.pg.application.pg.port.out.PgApproveResult
import im.bigs.pg.domain.payment.PaymentStatus
import im.bigs.pg.external.pg.AesGcmEncryptor
import im.bigs.pg.external.pg.ExternalPgException
import io.mockk.every
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest
import org.springframework.context.annotation.Import
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestConstructor
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers.*
import org.springframework.test.web.client.response.MockRestResponseCreators.withStatus
import org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.stream.Stream

@ActiveProfiles("test")
@RestClientTest(TestPgClient::class)
@Import(TestPgConfig::class)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@TestPropertySource("classpath:application-test.yaml")
class TestPgClientTest(
    @Value("\${external.pg.test.base-url}") private val baseUrl: String,
    @Value("\${external.pg.test.key}") private val apiKey: String,
    private val client: TestPgClient,
    private val mockServer: MockRestServiceServer,
    private val objectMapper: ObjectMapper,
) {

    @Test
    @DisplayName("결제가 승인되면 승인 정보를 반환한다")
    fun `결제가 승인되면 승인 정보를 반환한다`() {
        // given
        val approveRequest = validApproveRequest()
        val approvedAt = LocalDateTime.of(2025, 10, 8, 3, 31, 34)
        val approveResponse = TestPgApproveResponse(
            approvalCode = "10080728",
            approvedAt = approvedAt,
            maskedCardLast4 = "1111",
            amount= 10000,
            status = "APPROVED",
        )

        mockServer.expect(requestTo("$baseUrl/api/v1/pay/credit-card"))
            .andExpect(method(HttpMethod.POST))
            .andExpect(header("API-KEY", apiKey))
            .andExpect(jsonPath("enc").exists())
            .andRespond(withSuccess(
                objectMapper.writeValueAsString(approveResponse), MediaType.APPLICATION_JSON))

        // when
        val result = client.approve(approveRequest)

        // then
        mockServer.verify()
        with(result) {
            assertThat(status).isEqualTo(PaymentStatus.APPROVED)
            assertThat(approvalCode).isEqualTo("10080728")
            assertThat(approvedAt).isEqualTo(approvedAt)
        }
    }

    companion object {

        @JvmStatic
        fun invalidRequests(): Stream<Arguments> =
            Stream.of(
                invalid("카드번호 형식 오류 (16자리 미만)") {
                    copy(cardNumber = "1111-2222-3333")
                },
                invalid("생년월일 형식 오류 (8자리 아님)") {
                    copy(birthDate = "900101")
                },
                invalid("유효기간 형식 오류 (4자리 아님)") {
                    copy(expiry = "127")
                },
                invalid("비밀번호 형식 오류 (2자리 아님)") {
                    copy(password = "1")
                },
                invalid("금액 오류 (0원)") {
                    copy(amount = BigDecimal.ZERO)
                }
            )

        private fun invalid(
            description: String,
            mutate: PgApproveRequest.() -> PgApproveRequest
        ): Arguments =
            Arguments.of(description, validApproveRequest().mutate())

        private fun validApproveRequest(): PgApproveRequest =
            PgApproveRequest(
                amount = BigDecimal("10000"),
                password = "12",
                cardNumber = "1111-2222-3333-4444",
                expiry = "1227",
                birthDate = "19900101"
            )
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidRequests")
    @DisplayName("결제 요청 형식이 규칙과 다르면 예외를 발생시킨다")
    fun `결제 요청 형식이 규칙과 다르면 예외를 발생시킨다`(
        description: String,
        request: PgApproveRequest
    ) {
        // when & then
        assertThatThrownBy { TestPgApproveRequestBody.from(request) }
            .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    @DisplayName("결재 실패 시 예외를 발생시킨다")
    fun `결재 실패 시 예외를 발생시킨다`(){
        // given
        val approveRequest = validApproveRequest()

        val errorResponse = """
        {
          "code" : 1002,
          "errorCode" : "INSUFFICIENT_LIMIT",
          "message" : "한도가 초과되었습니다.",
          "referenceId" : "b48c79bd-e1b3-416a-a583-efe90d1ee438"
        }
    """.trimIndent()

        mockServer.expect(requestTo("$baseUrl/api/v1/pay/credit-card"))
            .andExpect(method(HttpMethod.POST))
            .andExpect(header("API-KEY", apiKey))
            .andExpect(jsonPath("enc").exists())
            .andRespond(
                withStatus(HttpStatus.valueOf(422))
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(errorResponse)
            )

        // when & then
        assertThatThrownBy { client.approve(approveRequest) }
            .isInstanceOf(ExternalPgException::class.java)

        mockServer.verify()
    }
}
