package jstack.checkpoint.resource

import jstack.checkpoint.KeyPart
import jstack.checkpoint.StepScope
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.createParentDirectories
import kotlin.io.path.div
import kotlin.reflect.KProperty

class FileSystemResourceProvider(private val basePath: Path): ResourceProvider<Serializable> {
    context(step: StepScope)
    override fun <T: Serializable> resource(keyPart: KeyPart): Resource<T> {
        val resourceKey = step.child(keyPart).key
        return FileSystemResource(resourceKey.joinToString("/"))
    }

    inner class FileSystemResource<T: Serializable>(relPath: String): Resource<T> {
        private val resourcePath = basePath / relPath
        override fun getValue(thisRef: Any?, property: KProperty<*>): T {
            return Files.newInputStream(resourcePath).use { inStream ->
                ObjectInputStream(inStream).readObject() as T
            }
        }

        override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
            Files.newOutputStream(resourcePath.createParentDirectories()).use { outStream ->
                ObjectOutputStream(outStream).writeObject(value)
            }
        }
    }
}

context(provider: FileSystemResourceProvider, _: StepScope)
fun <T: Serializable> fileSystemResource(key: KeyPart) = provider.resource<T>(key)
