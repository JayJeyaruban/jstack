package jstack.rpc.jdk

import jstack.di.DiContext
import jstack.di.Loader
import java.util.concurrent.Executors

val Executor = Loader<DiContext, java.util.concurrent.Executor> { Executors.newVirtualThreadPerTaskExecutor() }
