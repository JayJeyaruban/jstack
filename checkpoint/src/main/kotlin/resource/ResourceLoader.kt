package jstack.checkpoint.resource

import jstack.checkpoint.CheckpointScope
import jstack.checkpoint.Key
import jstack.checkpoint.resource.Resource
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable
import java.nio.file.Files
import java.nio.file.Path

interface ResourceLoader<in S> {
    fun <T: S> store(key: Key, resource: T): Result<Resource<T>>
}

abstract class ResourceLoaderBase<S>: ResourceLoader<S> {
    protected abstract fun <T: S> save(key: Key, resource: T)

    protected abstract fun <T: S> load(key: Key): T

    override fun <T : S> store(key: Key, resource: T): Result<Resource<T>> = runCatching {
        save(key, resource)
        ResourceImpl(key)
    }

    private inner class ResourceImpl<T>(private val key: Key): Resource<T> {
        override fun getValue(scope: CheckpointScope): Result<T> = runCatching {   load(key)}
    }
}

object FileSystemResourceLoader: ResourceLoaderBase<Serializable>() {
    override fun <T : Serializable> save(key: Key, resource: T) {
        Files.newOutputStream(Path.of(key)).use {
            val os = ObjectOutputStream(it)
            os.writeObject(resource)
        }
    }

    override fun <T : Serializable> load(key: Key): T {
        return Files.newInputStream(Path.of(key)).use {
            val inputStream = ObjectInputStream(it)
            inputStream.readObject() as T
        }
    }

//    fun <T: Serializable> CheckpointScope.store(key: Key, resource: T) = store(this@FileSystemResourceLoader, key, resource)
}
