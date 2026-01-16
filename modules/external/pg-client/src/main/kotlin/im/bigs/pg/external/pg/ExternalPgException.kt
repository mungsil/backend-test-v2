package im.bigs.pg.external.pg

import ExceptionStatus

class ExternalPgException(
    status: ExceptionStatus
) : RuntimeException(status.getMessage())