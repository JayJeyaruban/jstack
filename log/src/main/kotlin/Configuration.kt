package jstack.log

class Configuration(
    internal val logLevel: Level,
    private val _children: MutableMap<String, Configuration> = HashMap(),
) {
    internal val children get() = _children.toMap()

    companion object {
        fun fromEnv(envVariable: String = "JSTACK_LOG") = fromString(System.getenv()[envVariable] ?: "")

        fun fromString(configuration: String): Configuration {
            val pairs = configuration.split(",").filter { it.isNotEmpty() }.associate {
                val conf = it.split("=")
                conf[0] to conf[1]
            }.toMutableMap()

            val rootLevel = pairs.remove("ROOT")?.let { enumValueOf<Level>(it) } ?: Level.INFO
            val root = Configuration(rootLevel)
            pairs.forEach { (path, level) ->
                val parts = path.split(".")
                var node = root
                for (part in parts) {
                    if (part in node._children) {
                        node = node._children[part]!!
                        continue
                    }

                    node._children[part] = Configuration(enumValueOf(level))
                    break
                }
            }

            return root
        }
    }
}

fun Configuration.logLevel(callSite: CallSite): Level {
    val path = callSite.path
    var i = 0
    var node = this
    while (i < path.size && path[i] in node.children) {
        node = node.children[path[i]]!!
        i += 1
    }
    return node.logLevel
}
