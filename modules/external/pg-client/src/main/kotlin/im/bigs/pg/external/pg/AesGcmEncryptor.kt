package im.bigs.pg.external.pg

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Component
import java.nio.charset.Charset
import java.util.*
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

@Component
class AesGcmEncryptor(
    private val objectMapper: ObjectMapper
) {
    fun encrypt(
        key: ByteArray,
        iv: ByteArray,
        data: Any,
        cipherTransformation: String = "AES/GCM/NoPadding",
        keyAlgorithm: String = "AES",
        tagLengthBits: Int = 128,
        charset: Charset = Charsets.UTF_8
    ): ByteArray {
        val secretKey = SecretKeySpec(key, keyAlgorithm)

        val cipher = Cipher.getInstance(cipherTransformation)
        val spec = GCMParameterSpec(tagLengthBits, iv)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, spec)

        val plainText = objectMapper.writeValueAsString(data).toByteArray(charset)
        return cipher.doFinal(plainText)
    }
}
