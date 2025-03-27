package sample

import com.sun.net.httpserver.HttpServer
import jstack.di.DiContext
import jstack.di.register
import jstack.rpc.Router
import jstack.rpc.jdk.ConcurrentMapDiContext
import jstack.rpc.jdk.install
import jstack.rpc.router
import sample.common.JacksonCodec
import sample.routes.PetRouter
import sample.routes.Store
import java.net.InetSocketAddress

fun main() = with(ConcurrentMapDiContext()) {
    register(JacksonCodec)

    val server = HttpServer.create(InetSocketAddress(8080), 0)
    install(server, Server)
    server.start()
}

object Server: Router<DiContext>() {
    val pet by router(PetRouter)

    val store by router(Store)
}

