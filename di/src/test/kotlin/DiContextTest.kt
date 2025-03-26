package jstack.di

import jstack.core.Loader
import kotlin.test.Test
import kotlin.test.assertEquals

class DiContextTest {
    @Test
    fun contextTest() = with(DiContext()) {
        val a = retrieve(A)
        assertEquals("HelloHello", a.a())
    }

    @Test
    fun `extend and override`() = with(ExtendedContext(DiContext(), 1)) {
        val altB =
            Loader<ExtendedContext, B> {
                val mul = intProperty()
                object : B {
                    override fun b() = mul
                }
            }

        register(altB)

        val a = retrieve(A)
        assertEquals("Hello", a.a())
    }
}

interface A {
    fun a(): String

    companion object : Loader<DiContext, A> {
        override fun DiContext.load(): A {
            val b = retrieve(B)
            return object : A {
                override fun a() = "Hello".repeat(b.b())
            }
        }
    }
}

interface B {
    fun b(): Int

    companion object : Loader<DiContext, B> {
        override fun DiContext.load() = object : B {
            override fun b() = 2
        }
    }
}

interface ExtendedContext : DiContext {
    fun intProperty(): Int

    companion object {
        operator fun invoke(inner: DiContext, prop: Int): ExtendedContext = object : ExtendedContext, DiContext by inner {
            override fun intProperty() = prop
        }
    }
}
