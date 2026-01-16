package im.bigs.pg.domain.payment

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import kotlin.test.Test

class CardNumberTest {

    @Test
    @DisplayName("하이픈을 제거한 16자리 숫자 값을 가진다")
    fun `하이픈을 제거한 16자리 숫자 값을 가진다`() {
        val number = CardNumber.of("1212-1212-1212-1212")
        assertEquals("1212121212121212", number.value)
    }

    @Test
    @DisplayName("16자리가 아니라면 예외를 발생시킨다")
    fun `16자리가 아니라면 예외를 발생시킨다`() {
        Assertions.assertThatThrownBy {
            CardNumber.of("1212-1212-1212-212")
        }.isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    @DisplayName("카드 bin을 조회한다")
    fun `카드 bin을 조회한다`() {
        val number = CardNumber.of("1234-5678-9012-3456")
        val bin = number.getBin(8)
        assertEquals("12345678", bin)
    }

    @Test
    @DisplayName("카드 마지막 자릿수를 조회한다")
    fun `카드 마지막 자릿수를 조회한다`() {
        val number = CardNumber.of("1234-5678-9012-3456")
        val last = number.getLastN(4)
        assertEquals("3456", last)
    }

    @Test
    @DisplayName("음수이거나 16자리를 초과하면 예외를 발생시킨다")
    fun `음수이거나 16자리를 초과하면 예외를 발생시킨다`() {
        val number = CardNumber.of("1234-5678-9012-3456")

        Assertions.assertThatThrownBy {
            number.getBin(-1)
        }.isInstanceOf(IllegalArgumentException::class.java)

        Assertions.assertThatThrownBy {
            number.getLastN(17)
        }.isInstanceOf(IllegalArgumentException::class.java)
    }
}
