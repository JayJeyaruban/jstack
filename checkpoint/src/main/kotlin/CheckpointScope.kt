package jstack.checkpoint

import jstack.checkpoint.resource.Resource

fun <T> checkpoint(key: Key, persistence: CheckpointPersistence, f: CheckpointScope.() -> Resource<T>?) {
    val lastSteps = persistence.steps(key)
    val dsl = object: CheckpointScope {
        override fun <T : Any> step(
            key: Key,
            alwaysRun: Boolean,
            f: CheckpointScope.() -> Resource<T>?
        ): Resource<T>? {
            return if (alwaysRun) {
                f()
            } else {
                lastSteps[key] as Resource<T>
            }
        }
    }
}

interface CheckpointPersistence {
    fun steps(key: Key): Map<Key, Resource<*>>
}

interface CheckpointScope {
    fun <T: Any> step(key: Key, alwaysRun: Boolean, f: CheckpointScope.() -> Resource<T>?): Resource<T>?

//    fun <T: S, S> store(resourceLoader: ResourceLoader<S>, key: Key, resource: T): Resource<T>

    companion object {
        operator fun invoke(key: Key): CheckpointScope = CheckpointScopeImpl(key)
    }
}

fun <T: Any> CheckpointScope.step(key: Key, f: CheckpointScope.() -> Resource<T>?) = step(key, false, f)

internal class CheckpointScopeImpl(private val key: Key): CheckpointScope {

    override fun <T: Any> step(key: Key, alwaysRun: Boolean, f: CheckpointScope.() -> Resource<T>?): Resource<T>? = CheckpointScopeImpl(this.key + key).f()

//    override fun <T: S, S> store(
//        resourceLoader: ResourceLoader<S>,
//        key: Key,
//        resource: T
//    ): Resource<T> = resourceLoader.store(this.key + key, resource).getOrThrow()
}

//fun CheckpointScope.test() {
//    val thing = step("step1") {
//        store("key","hello world")
//    }
//
//    val thing2 = step("step2") {
//        val thing = thing.getValue(this)
//        store("key", "$thing foobar")
//    }
//
//    val result = thing2.getValue(this)
//}
