package jstack.core

fun interface Loader<in Ctx, out M> {
    fun Ctx.load(): M
}

fun <Ctx, T> Ctx.load(other: Loader<Ctx, T>): T = other.run { load() }
