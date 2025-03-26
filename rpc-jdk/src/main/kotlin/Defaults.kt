package jstack.rpc.jdk

import jstack.core.Loader
import java.util.concurrent.Executor
import java.util.concurrent.Executors

val Executor = jstack.core.Loader<Any, Executor> { Executors.newVirtualThreadPerTaskExecutor() }
