# jstack

A collection of libraries to compose your own framework.

## Modules

### DI

A dependency injection library leveraging extension functions.
See the [test](/di/src/test/kotlin/DiContextTest.kt) for example usage.

### RPC

A zero-dependency, no-codegen RPC library where services are defined as singleton objects.

#### RPC-JDK

An RPC over HTTP implementation built on the RPC module and the standard library HTTP server and client.
See the [test](/rpc-jdk/src/test/kotlin/ClientServerTest.kt) for example usage.
