package jstack.core

fun interface Loader<in Ctx, out M> {
    fun Ctx.load(): M
}
