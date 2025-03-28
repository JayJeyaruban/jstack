package jstack.rpc.jdk

import jstack.core.Loader
import jstack.core.Type
import jstack.di.DiContext
import java.io.InputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.OutputStream

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
