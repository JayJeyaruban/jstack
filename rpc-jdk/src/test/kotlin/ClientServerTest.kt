package jstack.rpc.jdk

import com.sun.net.httpserver.HttpServer
import jstack.core.Loader
import jstack.di.SynchronizedMapBasedDiContext
import jstack.di.DiContext
import jstack.di.retrieve
import jstack.rpc.Router
import jstack.rpc.procedure
import jstack.rpc.router
import java.net.InetSocketAddress
import kotlin.test.Test
import kotlin.test.assertEquals

private const val PORT = 8080

class ClientServerTest {
    @Test
    fun test() = with(SynchronizedMapBasedDiContext()) {
        val ex = retrieve(Executor)
        ex.execute {
            val server = HttpServer.create(InetSocketAddress(PORT), 0)
            install(server, Example)
            server.start()
        }

        val input = "Hello world!"

        val client = retrieve(TestClient)
        assertEquals(input, client.call { nested.echo }(input))
    }
}

object Nested : Router<DiContext>() {
    val echo by procedure { input: String -> input }
}

object Example : Router<DiContext>() {
    val nested by router(Nested)
}

val TestClient = Loader<DiContext, Client<DiContext, Example>> { client(Example, "http://localhost:$PORT") }
