package jstack.rpc

import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

typealias Procedure<I, O> = (I) -> O

abstract class Router {
    internal val routes = mutableListOf<Route>()
}

inline fun <reified I, reified O> procedure(noinline proc: Procedure<I, O>) = ProcedureRouteLoader(proc, I::class.java, O::class.java)

fun <R : Router> router(router: R) = RouterRouteLoader(router)

fun Router.traverse(f: (path: String, proc: ProcedureRoute<*, *>) -> Unit) {
    val segments = mutableListOf<String>()

    fun dfs(router: Router) {
        router.routes.forEach { route ->
            when (route) {
                is ProcedureRoute<*, *> -> {
                    segments.add(route.path)
                    val path = segments.joinToString("/")
                    f(path, route)
                    segments.removeLast()
                }
                is RouterRoute<*> -> {
                    segments.add(route.path)
                    dfs(route.router)
                    segments.removeLast()
                }
            }
        }
    }

    dfs(this)
}

internal sealed interface Route

class ProcedureRoute<I, O>(
    internal val path: String,
    private val proc: Procedure<I, O>,
    val input: Class<I>,
    val output: Class<O>,
) : Route, Procedure<I, O> by proc

class ProcedureRouteLoader<I, O>(private val proc: Procedure<I, O>, private val input: Class<I>, private val output: Class<O>) {
    operator fun provideDelegate(
        thisRef: Router,
        prop: KProperty<*>,
    ): ReadOnlyProperty<Router, ProcedureRoute<I, O>> {
        val wrapped = ProcedureRoute(prop.name, proc, input, output)
        thisRef.routes.add(wrapped)
        return ReadOnlyProperty { _, _ -> wrapped }
    }
}

class RouterRoute<R : Router>(internal val path: String, internal val router: R) : Route, ReadOnlyProperty<Router, R> {
    override fun getValue(
        thisRef: Router,
        property: KProperty<*>,
    ) = router
}

class RouterRouteLoader<R : Router>(private val router: R) {
    operator fun provideDelegate(
        thisRef: Router,
        prop: KProperty<*>,
    ) = RouterRoute(prop.name, router).also {
        thisRef.routes.add(it)
    }
}
