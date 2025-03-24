package jstack.rpc.jdk

import com.sun.net.httpserver.HttpServer
import jstack.di.DiContext
import jstack.di.Loader
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
    fun test() =
        with(DiContext()) {
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

object Nested : Router() {
    val echo by procedure<String, String> { it }
}

object Example : Router() {
    val nested by router(Nested)
}

val TestClient = Loader<DiContext, Client<Example>> { client(Example, "http://localhost:$PORT") }
