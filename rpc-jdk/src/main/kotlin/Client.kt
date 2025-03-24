package jstack.rpc.jdk

import jstack.di.DiContext
import jstack.di.Loader
import jstack.di.retrieve
import jstack.rpc.Procedure
import jstack.rpc.ProcedureRoute
import jstack.rpc.Router
import jstack.rpc.traverse
import java.io.ByteArrayOutputStream
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpRequest.BodyPublishers
import java.net.http.HttpResponse.BodyHandler
import java.net.http.HttpResponse.BodySubscribers

class Client<R : Router> internal constructor(
    private val topRouter: R,
    private val client: HttpClient,
    private val codec: Codec,
    private val baseUrl: String,
    private val routes: Map<Procedure<*, *>, String>,
) {
    fun <I, O> call(f: R.() -> ProcedureRoute<I, O>): Procedure<I, O> {
        val proc = topRouter.f()
        val path = routes[proc]!!
        return { input ->
            println(path)
            client.send(
                HttpRequest.newBuilder()
                    .uri(URI.create("$baseUrl/$path/"))
                    .POST(codec.publisher(input))
                    .build(),
                codec.handler(proc.output),
            ).body()
        }
    }
}

fun interface ClientFactory {
    fun clientBuilder(): HttpClient.Builder

    companion object : Loader<DiContext, ClientFactory> {
        override fun DiContext.load() =
            ClientFactory {
                HttpClient.newBuilder()
                    .executor(retrieve(Executor))
            }
    }
}

fun <R : Router> DiContext.client(
    router: R,
    baseUrl: String,
): Client<R> {
    val routes: Map<Procedure<*, *>, String> =
        buildMap {
            router.traverse { path, proc ->
                put(proc, path)
            }
        }

    val client = retrieve(ClientFactory).clientBuilder().build()
    return Client(router, client, retrieve(Codec), baseUrl, routes)
}

private fun Codec.publisher(input: Any?): HttpRequest.BodyPublisher? {
    val bytes =
        ByteArrayOutputStream().use {
            write(it, input)
            it.toByteArray()
        }
    return BodyPublishers.ofByteArray(bytes)
}

private fun <T> Codec.handler(t: Class<T>): BodyHandler<T> {
    return BodyHandler {
        BodySubscribers.mapping(BodySubscribers.ofInputStream()) { input ->
            read(input, t)
        }
    }
}
