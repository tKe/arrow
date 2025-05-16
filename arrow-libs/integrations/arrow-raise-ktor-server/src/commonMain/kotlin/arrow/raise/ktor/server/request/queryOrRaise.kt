@file:JvmName("RequestRaise")
@file:JvmMultifileClass

package arrow.raise.ktor.server.request

import arrow.core.raise.Raise
import arrow.raise.ktor.server.Response
import arrow.raise.ktor.server.raisingErrorResponse
import io.ktor.server.routing.*
import io.ktor.util.reflect.*
import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName

context(r: Raise<RequestError>)
@JvmName("queryOrRaiseReified")
public inline fun <reified A : Any> RoutingCall.queryOrRaise(name: String): A =
  queryParameters.parameterOrRaise(Parameter.Query(name), typeInfo<A>())

context(r: Raise<RequestError>)
public inline fun <A : Any> RoutingCall.queryOrRaise(name: String, transform: Raise<String>.(String) -> A): A =
  queryParameters.parameterOrRaise(Parameter.Query(name), transform)

context(r: Raise<RequestError>)
public fun RoutingCall.queryOrRaise(name: String): String =
  queryParameters.parameterOrRaise(Parameter.Query(name))

context(r: Raise<Response>)
@JvmName("queryOrRaiseReified")
public inline fun <reified A : Any> RoutingContext.queryOrRaise(name: String): A =
  raisingErrorResponse { call.queryOrRaise<A>(name) }

context(r: Raise<Response>)
public inline fun <A : Any> RoutingContext.queryOrRaise(name: String, transform: Raise<String>.(String) -> A): A =
  raisingErrorResponse { call.queryOrRaise(name, transform) }

context(r: Raise<Response>)
public fun RoutingContext.queryOrRaise(name: String): String =
  raisingErrorResponse { call.queryOrRaise(name) }

context(c: RoutingContext)
@JvmName("queryOrRaiseReified")
public inline fun <reified A : Any> Raise<RequestError>.queryOrRaise(name: String): A =
  c.call.queryOrRaise<A>(name)

context(c: RoutingContext)
public inline fun <A : Any> Raise<RequestError>.queryOrRaise(name: String, transform: Raise<String>.(String) -> A): A =
  c.call.queryOrRaise(name, transform)

context(c: RoutingContext)
public fun Raise<RequestError>.queryOrRaise(name: String): String =
  c.call.queryOrRaise(name)
