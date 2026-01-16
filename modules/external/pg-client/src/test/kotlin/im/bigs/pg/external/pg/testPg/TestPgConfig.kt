package im.bigs.pg.external.pg.testPg

import com.fasterxml.jackson.databind.ObjectMapper
import im.bigs.pg.external.pg.AesGcmEncryptor
import io.mockk.mockk
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.web.client.RestClient

@TestConfiguration
class TestPgConfig {

    @Bean
    fun testPgClient(
        aesGcmEncryptor: AesGcmEncryptor,
        testPgErrorHandler: TestPgErrorHandler,
        restClientBuilder: RestClient.Builder,
        @Value("\${external.pg.test.key}")
        apiKey: String,
        @Value("\${external.pg.test.base-url}")
        baseUrl: String,
        @Value("\${external.pg.test.iv}")
        iv: String,
        ): TestPgClient {
        return TestPgClient(
            apiKey = apiKey,
            baseUrl = baseUrl,
            ivBase64Url = iv,
            testPgErrorHandler = testPgErrorHandler,
            restClientBuilder = restClientBuilder,
            encryptor = aesGcmEncryptor,
        )
    }

    @Bean
    fun testPgReqBodyEncryptor(
        jsonMapper: ObjectMapper,
    ): AesGcmEncryptor {
        return AesGcmEncryptor(jsonMapper)
    }

    @Bean
    fun testPgErrorHandler(
        jsonMapper: ObjectMapper,
    ): TestPgErrorHandler {
        return TestPgErrorHandler(jsonMapper)
    }
}
