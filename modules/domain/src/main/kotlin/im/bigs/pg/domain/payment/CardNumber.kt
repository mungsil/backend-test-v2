package im.bigs.pg.domain.payment

 class CardNumber private constructor(
    val value: String
){
    /**
     * 카드 BIN 조회
     * @param len BIN 길이
     */
    fun getBin(len: Int): String {
        require(len > 0 && len <= value.length) {
            "bin length must be between 1 and ${value.length}"
        }
        return value.substring(0, len)
    }

    /**
     * 카드 마지막 N자리 조회
     * @param len 자리 수
     */
    fun getLastN(len: Int): String {
        require(len > 0 && len <= value.length) {
            "last digit length must be between 1 and ${value.length}"
        }
        return value.substring(value.length - len)
    }

    companion object {
        fun of(raw: String): CardNumber {
            val normalized = raw.replace("-", "")

            require(normalized.length == 16) {
                "cardNumber must be exactly 16 digits"
            }
            require(normalized.all { it.isDigit() }) {
                "cardNumber must contain digits only"
            }

            return CardNumber(normalized)
        }
    }
}