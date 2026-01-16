package im.bigs.pg.external.pg.testPg

import im.bigs.pg.application.pg.port.out.PgApproveRequest
import im.bigs.pg.application.pg.port.out.PgApproveResult
import im.bigs.pg.application.pg.port.out.PgClientOutPort
import im.bigs.pg.external.pg.AesGcmEncryptor
import im.bigs.pg.external.pg.ExternalPgException
import im.bigs.pg.external.pg.ExternalPgExceptionStatus
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders.CONTENT_TYPE
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import java.security.MessageDigest
import java.util.*

@Component
class TestPgClient(
    @Value("\${external.pg.test.key}")
    private val apiKey: String,
    @Value("\${external.pg.test.iv}")
    private val ivBase64Url: String,
    @Value("\${external.pg.test.base-url}")
    private val baseUrl: String,
    private val encryptor: AesGcmEncryptor,
    private val testPgErrorHandler: TestPgErrorHandler,
    private val restClientBuilder: RestClient.Builder
) : PgClientOutPort {

    private val logger = LoggerFactory.getLogger(javaClass)

    // TestPg 암호화 스펙
    companion object {
        private const val TAG_LENGTH_BITS = 128
        private const val HASH_ALGORITHM = "SHA-256"
        private const val KEY_ALGORITHM = "AES"
        private const val CIPHER_TRANSFORMATION = "AES/GCM/NoPadding"
        private val CHARSET = Charsets.UTF_8
        private val BASE64_ENCODER = Base64.getUrlEncoder().withoutPadding()
    }

    private val hashedKey: ByteArray = MessageDigest.getInstance(HASH_ALGORITHM)
        .digest(apiKey.toByteArray(CHARSET))

    private val decodedIv: ByteArray = Base64.getUrlDecoder().decode(ivBase64Url)

    private val restClient = restClientBuilder
        .baseUrl(baseUrl)
        .defaultHeader(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .defaultHeader("API-KEY", apiKey)
        .build()

    override fun supports(partnerId: Long): Boolean = partnerId == 2L

    override fun approve(request: PgApproveRequest): PgApproveResult {
        val body = TestPgApproveRequestBody.from(request)
        val encBody = encrypt(body)

        val res = restClient.post()
            .uri("/api/v1/pay/credit-card")
            .body(encBody)
            .retrieve()
            .onStatus(testPgErrorHandler)
            .body(TestPgApproveResponse::class.java)
            ?: run {
                logger.error("PG 결제 승인 응답이 null입니다.")
                throw ExternalPgException(ExternalPgExceptionStatus.UNKNOWN_ERROR)
            }

        return res.toPgResult()
    }

    private fun encrypt(body: TestPgApproveRequestBody): RequestBody {
        val encrypted = encryptor.encrypt(
            key = hashedKey,
            iv = decodedIv,
            data = body,
            cipherTransformation = CIPHER_TRANSFORMATION,
            keyAlgorithm = KEY_ALGORITHM,
            tagLengthBits = TAG_LENGTH_BITS,
            charset = CHARSET
        )

        return RequestBody(BASE64_ENCODER.encodeToString(encrypted))
    }

    data class RequestBody(val enc: String)

    data class ErrorResponse(
        val code: Number,
        val errorCode: String,
        val message: String,
        val referenceId: String,
    )
}
