package im.bigs.pg.external.pg

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.test.context.ActiveProfiles
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

@ActiveProfiles("test")
class AesGcmEncryptorTest() {

    private val objectMapper = jacksonObjectMapper()
    private val aesGcmEncryptor = AesGcmEncryptor(objectMapper)

    @Test
    @DisplayName("암호화된 데이터를 복호화하면 원본과 일치해야한다")
    fun `암호화된 데이터를 복호화하면 원본과 일치해야한다`() {
        // given
        val key = generateAesKey(256)
        val iv = generateRandomBytes(12)
        val data = "Hello World"

        // when
        val encrypted = aesGcmEncryptor.encrypt(
            key = key,
            iv = iv,
            data = data,
            cipherTransformation = "AES/GCM/NoPadding",
            keyAlgorithm = "AES",
            tagLengthBits = 128,
            charset = Charsets.UTF_8
        )

        // then
        val decrypted = decrypt(
            key = key,
            iv = iv,
            encrypted = encrypted,
            cipherTransformation = "AES/GCM/NoPadding",
            keyAlgorithm = "AES",
            tagLengthBits = 128
        )
        val decryptedString = String(decrypted, Charsets.UTF_8)
        val decryptedData = objectMapper.readValue(decryptedString, String::class.java)

        assertEquals(data, decryptedData)
    }

    private fun decrypt(
        key: ByteArray,
        iv: ByteArray,
        encrypted: ByteArray,
        cipherTransformation: String = "AES/GCM/NoPadding",
        keyAlgorithm: String = "AES",
        tagLengthBits: Int = 128
    ): ByteArray {
        val secretKey = SecretKeySpec(key, keyAlgorithm)
        val cipher = Cipher.getInstance(cipherTransformation)
        val spec = GCMParameterSpec(tagLengthBits, iv)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)
        return cipher.doFinal(encrypted)
    }

    private fun generateAesKey(bits: Int = 256): ByteArray {
        val keyGen = KeyGenerator.getInstance("AES")
        keyGen.init(bits)
        return keyGen.generateKey().encoded
    }

    private fun generateRandomBytes(size: Int): ByteArray {
        val bytes = ByteArray(size)
        SecureRandom().nextBytes(bytes)
        return bytes
    }
}
