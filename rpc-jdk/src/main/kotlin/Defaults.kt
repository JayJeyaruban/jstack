package jstack.rpc.jdk

import jstack.core.Loader
import java.util.concurrent.Executor
import java.util.concurrent.Executors

val Executor = Loader<Any, Executor> { Executors.newVirtualThreadPerTaskExecutor() }
