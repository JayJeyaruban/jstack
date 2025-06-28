package jstack.checkpoint

import jstack.checkpoint.resource.FileSystemResourceProvider
import jstack.checkpoint.resource.fileSystemResource
import jstack.log.EventConsumer
import jstack.log.error
import jstack.log.info
import jstack.log.message
import jstack.log.setAttribute
import java.nio.file.Path

typealias StepScopeFn<T> = context(StepScope)() -> T
typealias Step = StepScopeFn<Unit>

context(ctx: CheckpointContext)
inline fun <T> checkpoint(key: KeyPart, block: StepScopeFn<T>) : T = StepScope(listOf(key)).run(block)

context(step: StepScope, ctx: CheckpointContext, log: EventConsumer)
inline fun step(key: KeyPart, block: Step) {
    val next = step.child(key)
    val lastRun = ctx.report(next.key)
    if (lastRun != Outcome.SUCCESS) {
        try {
            next.run(block)
            ctx.record(next.key, Outcome.SUCCESS)
        } catch (e: Exception) {
            log.info {
                message("Exception caught in step")
                setAttribute("key", next.key)
                error(e)
            }
            ctx.record(next.key, Outcome.FAILURE)
        }
    } else {
        ctx.record(next.key, Outcome.SUCCESS)
    }
}

interface CheckpointContext {
    fun report(key: Key): Outcome?

    fun record(key: Key, outcome: Outcome)
}

        enum class Outcome {
            SUCCESS,
            FAILURE
        }

interface IStepScope {
    val key: Key
}

class SimpleStepScope (override val key: Key): IStepScope

fun IStepScope.child(key: KeyPart): IStepScope = SimpleStepScope(this.key.append(key))

class StepScope(val key: Key) {
    fun child(key: KeyPart) = StepScope(this.key.append(key))
}

context(_: StepScope, _: CheckpointContext, _: FileSystemResourceProvider, _: EventConsumer)
fun test(i: Int) {
    var res by fileSystemResource<String>("hello")
    step("step1") {
        println("Hello")
        step("nested$i") {
            println("Foo")
            step("nested3") {
                res = "MY WORLD"
                println("BAR")
            }
        }
    }

    step("step2") {
        println("World")
        println(res)
    }
}

fun main() = MapImpl().run {
    context(FileSystemResourceProvider(Path.of("stuff")), EventConsumer.stderr()) {
        checkpoint("foo") {
            test(0)
        }

        println(map)

        checkpoint("foo") {
            test(1)
        }
    }
}

class MapImpl(val map: HashMap<Key, Outcome> = HashMap()): CheckpointContext {
    override fun report(key: Key): Outcome? = map[key]

    override fun record(key: Key, outcome: Outcome) {
        map[key] = outcome
    }
}

