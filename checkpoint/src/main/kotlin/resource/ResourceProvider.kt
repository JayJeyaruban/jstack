package jstack.checkpoint.resource

import jstack.checkpoint.KeyPart
import jstack.checkpoint.StepScope
import kotlin.properties.ReadWriteProperty

interface ResourceProvider<Serde> {
    context(step: StepScope)
    fun <T: Serde> resource(keyPart: KeyPart): Resource<T>
}

interface Resource<T>: ReadWriteProperty<Any?, T>
