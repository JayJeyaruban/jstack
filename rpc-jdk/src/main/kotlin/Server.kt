package jstack.rpc.jdk

import com.sun.net.httpserver.Filter
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import jstack.di.DiContext
import jstack.di.retrieve
import jstack.log.LogContext
import jstack.log.logger
import jstack.log.trace
import jstack.rpc.ProcedureRoute
import jstack.rpc.Router
import jstack.rpc.traverse

fun <C> C.install(
    server: HttpServer,
    router: Router<C>,
) where C: DiContext, C: LogContext = server.apply {
    val log by logger()
    executor = retrieve(Executor)

    val codec = retrieve(Codec)

    router.traverse { path, proc ->
        createContext("/$path/") { ex ->
            if (ex.requestMethod == "POST") {
                execute(proc, codec, ex)
            } else {
                ex.sendResponseHeaders(405, -1)
            }

            ex.close()
        }.apply {
            filters.add(
                Filter.beforeHandler("log") {
                    log.trace {
                        put("method", it.requestMethod)
                        put("uri", it.requestURI)
                    }
                },
            )
        }
    }
}

private fun <C : DiContext, I, O> C.execute(
    proc: ProcedureRoute<C, I, O>,
    codec: Codec,
    ex: HttpExchange,
) {
    try {
        val args = codec.read(ex.requestBody, proc.input)
        val response = proc(this)(args)
        ex.sendResponseHeaders(200, 0)
        codec.write(ex.responseBody, response)
    } catch (e: Throwable) {
        e.printStackTrace()
        ex.sendResponseHeaders(500, -1)
    }
}
