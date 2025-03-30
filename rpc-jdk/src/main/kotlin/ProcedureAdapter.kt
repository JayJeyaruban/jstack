package jstack.rpc.jdk

import com.sun.net.httpserver.HttpHandler
import jstack.core.Loader
import jstack.core.Type
import jstack.di.DiContext
import jstack.di.retrieve
import jstack.rpc.ProcedureRoute
import java.io.InputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.OutputStream

interface ProcedureAdapter {
    fun <C, I, O> C.adapt(proc: ProcedureRoute<C, I, O>): HttpHandler

    companion object : Loader<DiContext, ProcedureAdapter> {
        override fun DiContext.load(): ProcedureAdapter {
            val codec = retrieve(Codec)
            return object : ProcedureAdapter {
                override fun <C, I, O> C.adapt(proc: ProcedureRoute<C, I, O>) = HttpHandler { ex ->
                    if (ex.requestMethod == "POST") {
                        try {
                            val args = codec.read(ex.requestBody, proc.input)
                            val response = proc(this)(args)
                            ex.sendResponseHeaders(200, 0)
                            codec.write(ex.responseBody, response)
                        } catch (e: Throwable) {
                            ex.sendResponseHeaders(500, -1)
                        }
                    } else {
                        ex.sendResponseHeaders(405, -1)
                    }

                    ex.close()
                }
            }
        }
    }
}

interface Codec {
    fun <T> read(inputStream: InputStream, type: Type<T>): T

    fun <T> write(outputStream: OutputStream, t: T)

    companion object : Loader<DiContext, Codec> {
        override fun DiContext.load(): Codec = SerializableCodec
    }
}

internal object SerializableCodec : Codec {
    override fun <T> read(inputStream: InputStream, type: Type<T>): T {
        return type.cast(ObjectInputStream(inputStream).readObject())
    }

    override fun <T> write(outputStream: OutputStream, t: T) {
        ObjectOutputStream(outputStream).writeObject(t)
    }
}
