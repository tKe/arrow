@file:JvmName("RequestRaise")
@file:JvmMultifileClass

package arrow.raise.ktor.server.request

import arrow.core.raise.Raise
import arrow.raise.ktor.server.Response
import arrow.raise.ktor.server.raiseError
import io.ktor.server.routing.*
import io.ktor.util.reflect.*
import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName

context(_: Raise<RequestError>)
@JvmName("pathOrRaiseReified")
public inline fun <reified A : Any> RoutingCall.pathOrRaise(name: String): A = pathParameters.parameterOrRaise(Parameter.Path(name), typeInfo<A>())

context(_: Raise<RequestError>)
public inline fun <A : Any> RoutingCall.pathOrRaise(name: String, transform: ParameterTransform<A>): A = pathParameters.parameterOrRaise(Parameter.Path(name), transform)

context(_: Raise<RequestError>)
public fun RoutingCall.pathOrRaise(name: String): String = pathParameters.parameterOrRaise(Parameter.Path(name))

context(r: Raise<Response>)
@JvmName("pathOrRaiseReified")
public inline fun <reified A : Any> RoutingContext.pathOrRaise(name: String): A = raiseError { call.pathOrRaise<A>(name) }

context(r: Raise<Response>)
public inline fun <A : Any> RoutingContext.pathOrRaise(name: String, transform: ParameterTransform<A>): A = raiseError { call.pathOrRaise(name, transform) }

context(r: Raise<Response>)
public fun RoutingContext.pathOrRaise(name: String): String = raiseError { call.pathOrRaise(name) }
