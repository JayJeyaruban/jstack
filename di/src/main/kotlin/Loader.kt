package jstack.di

fun interface Loader<Ctx, M> {
    fun Ctx.load(): M
}
