package jstack.log

interface CallSite {
    val path: List<String>

    companion object {
        fun walkBack(frames: Long = 2): CallSite = StackWalker.getInstance().walk { stack ->
            val frame = stack.skip(frames).findFirst().get()
            StackFrameCallSite(frame)
        }
    }
}

val CallSite.fullPath get() = path.joinToString(".")

internal class StackFrameCallSite(private val element: StackWalker.StackFrame) : CallSite {
    override val path: List<String> by lazy {
        buildList {
            element.className.split(".").forEach { add(it) }

            if (element.methodName != "<init>") {
                add(element.methodName)
            }
        }
    }

    override fun equals(other: Any?): Boolean = other?.let {
        if (it is StackFrameCallSite) {
            it.element == element
        } else {
            null
        }
    } ?: false

    override fun hashCode() = element.hashCode()
}
