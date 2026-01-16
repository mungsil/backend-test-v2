package im.bigs.pg.external.pg.testPg

import com.fasterxml.jackson.annotation.JsonFormat
import im.bigs.pg.application.pg.port.out.PgApproveResult
import java.time.LocalDateTime

data class TestPgApproveResponse(
    val approvalCode: String,
    val approvedAt: LocalDateTime,
    val maskedCardLast4: String,
    val amount: Number,
    val status: String,
){
    fun toPgResult() : PgApproveResult {
        return PgApproveResult(
            approvalCode = approvalCode,
            approvedAt = approvedAt,
            )
    }
}
