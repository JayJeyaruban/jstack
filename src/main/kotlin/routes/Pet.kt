package sample.routes

import jstack.di.DiContext
import jstack.log.debug
import jstack.log.info
import jstack.log.logger
import jstack.log.message
import jstack.rpc.Router
import jstack.rpc.procedure
import sample.internal.petFindByStatus
import sample.models.Status

object PetRouter: Router<DiContext>() {
    val findByStatus by procedure { statuses: List<Status> ->
        require(statuses.isNotEmpty()) { "Need at least one status" }
        val log by logger()
        log.debug { put("statuses", statuses.toString()) }

        log.info { message("Making request") }
        petFindByStatus(statuses)
    }
}