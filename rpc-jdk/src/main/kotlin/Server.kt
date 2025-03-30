package jstack.rpc.jdk

import com.sun.net.httpserver.HttpContext
import com.sun.net.httpserver.HttpServer
import jstack.di.DiContext
import jstack.di.retrieve
import jstack.rpc.Router
import jstack.rpc.traverse

typealias HttpContextConfigurer = HttpContext.() -> Unit

fun <C : DiContext> C.install(server: HttpServer, router: Router<C>, f: HttpContextConfigurer? = null) = server.apply {
    executor = retrieve(Executor)

    val adapter = retrieve(ProcedureAdapter)
    router.traverse { path, proc ->
        createContext("/$path/", adapter.run { adapt(proc) }).apply { f?.invoke(this) }
    }
}
