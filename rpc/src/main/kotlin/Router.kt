package jstack.rpc

import jstack.core.Type
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

typealias Procedure<C, I, O> = C.(I) -> O

abstract class Router<C> {
    internal val procedures = mutableListOf<ProcedureRoute<C, *, *>>()
    internal val subRouters = mutableListOf<RouterRoute<C, Router<C>>>()
}

inline fun <C, reified I, reified O> procedure(noinline proc: Procedure<C, I, O>) = ProcedureRouteLoader(proc, Type.of(), Type.of())

fun <C, R : Router<C>> router(router: R) = RouterRouteLoader(router)

fun <C> Router<C>.traverse(f: (path: String, proc: ProcedureRoute<C, *, *>) -> Unit) {
    val segments = mutableListOf<String>()

    fun dfs(router: Router<C>) {
        router.procedures.forEach { route ->
            segments.add(route.path)
            val path = segments.joinToString("/")
            f(path, route)
            segments.removeLast()
        }

        router.subRouters.forEach { route ->
            segments.add(route.path)
            dfs(route.router)
            segments.removeLast()
        }
    }

    dfs(this)
}

class ProcedureRoute<in C, I, O>(
    internal val path: String,
    private val proc: Procedure<C, I, O>,
    val input: Type<I>,
    val output: Type<O>,
) {
    operator fun invoke(c: C): (I) -> O = { c.proc(it) }
}

class ProcedureRouteLoader<C, I, O>(
    private val proc: Procedure<C, I, O>,
    private val input: Type<I>,
    private val output: Type<O>,
) {
    operator fun provideDelegate(
        thisRef: Router<C>,
        prop: KProperty<*>,
    ): ReadOnlyProperty<Router<C>, ProcedureRoute<C, I, O>> {
        val wrapped = ProcedureRoute(prop.name, proc, input, output)
        thisRef.procedures.add(wrapped)
        return ReadOnlyProperty { _, _ -> wrapped }
    }
}

class RouterRoute<C, out R : Router<C>>(internal val path: String, internal val router: R) : ReadOnlyProperty<Router<C>, R> {
    override fun getValue(
        thisRef: Router<C>,
        property: KProperty<*>,
    ) = router
}

class RouterRouteLoader<C, R : Router<C>>(private val router: R) {
    operator fun provideDelegate(
        thisRef: Router<C>,
        prop: KProperty<*>,
    ) = RouterRoute(prop.name, router).also {
        thisRef.subRouters.add(it)
    }
}
