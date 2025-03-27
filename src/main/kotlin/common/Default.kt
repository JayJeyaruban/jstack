package sample.common

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import jstack.core.Loader
import jstack.core.Type
import jstack.di.DiContext
import jstack.di.retrieve
import jstack.rpc.jdk.Codec
import jstack.rpc.jdk.Executor
import java.io.InputStream
import java.io.OutputStream
import java.net.http.HttpClient
import kotlin.reflect.jvm.javaType

val Client = Loader<DiContext, HttpClient> { HttpClient.newBuilder().executor(retrieve(Executor)).build() }

val ObjectMapper = Loader<Any, ObjectMapper> { jacksonObjectMapper() }

class JacksonCodec(private val objectMapper: ObjectMapper): Codec {
    override fun <T> read(inputStream: InputStream, type: Type<T>): T = objectMapper.readValue(inputStream, type.typeRef())

    override fun <T> write(outputStream: OutputStream, t: T) = objectMapper.writeValue(outputStream, t)

    companion object: Loader<DiContext, Codec> {
        override fun DiContext.load(): Codec = JacksonCodec(retrieve(ObjectMapper))
    }
}

fun <T> Type<T>.typeRef(): TypeReference<T> = object : TypeReference<T>() {
    override fun getType(): java.lang.reflect.Type {
        return t.javaType
    }
}
