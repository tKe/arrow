@file:JvmName("RequestRaise")
@file:JvmMultifileClass

package arrow.raise.ktor.server.request

import arrow.core.raise.Raise
import arrow.raise.ktor.server.RaiseRoutingContext
import arrow.raise.ktor.server.raiseError
import io.ktor.server.routing.RoutingCall
import io.ktor.util.reflect.*
import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName

@JvmName("queryOrRaiseReified")
public inline fun <reified A : Any> Raise<RequestError>.queryOrRaise(call: RoutingCall, name: String): A =
  parameterOrRaise(call.queryParameters, Parameter.Query(name), typeInfo<A>())

public inline fun <A : Any> Raise<RequestError>.queryOrRaise(call: RoutingCall, name: String, transform: Raise<String>.(String) -> A): A =
  parameterOrRaise(call.queryParameters, Parameter.Query(name), transform)

public fun Raise<RequestError>.queryOrRaise(call: RoutingCall, name: String): String =
  parameterOrRaise(call.queryParameters, Parameter.Query(name))

@JvmName("queryOrRaiseReified")
public inline fun <reified A : Any> RaiseRoutingContext.queryOrRaise(name: String): A =
  raiseError { queryOrRaise<A>(call, name) }

public inline fun <A : Any> RaiseRoutingContext.queryOrRaise(name: String, transform: Raise<String>.(String) -> A): A =
  raiseError { queryOrRaise(call, name, transform) }

public fun RaiseRoutingContext.queryOrRaise(name: String): String =
  raiseError { queryOrRaise(call, name) }
