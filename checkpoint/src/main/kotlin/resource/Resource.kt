package jstack.checkpoint.resource

import jstack.checkpoint.CheckpointScope

interface Resource<out T> {
    fun getValue(scope: CheckpointScope): Result<T>
}