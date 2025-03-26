package jstack.rpc.jdk

import com.sun.net.httpserver.Filter
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import jstack.di.DiContext
import jstack.di.retrieve
import jstack.rpc.ProcedureRoute
import jstack.rpc.Router
import jstack.rpc.traverse

fun <C : DiContext> C.install(
    server: HttpServer,
    router: Router<C>,
) = server.apply {
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
                    println("${it.requestMethod} => ${it.requestURI}")
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
