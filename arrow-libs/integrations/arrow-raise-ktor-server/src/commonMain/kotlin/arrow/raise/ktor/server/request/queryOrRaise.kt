@file:JvmName("RequestRaise")
@file:JvmMultifileClass

package arrow.raise.ktor.server.request

import arrow.core.raise.Raise
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
