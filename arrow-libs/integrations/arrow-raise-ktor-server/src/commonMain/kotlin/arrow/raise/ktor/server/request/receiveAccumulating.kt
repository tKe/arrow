package arrow.raise.ktor.server.request

import arrow.core.raise.ExperimentalRaiseAccumulateApi
import arrow.core.raise.RaiseAccumulate
import io.ktor.server.routing.*
import io.ktor.util.reflect.*

context(ra: RaiseAccumulate<RequestError>)
@ExperimentalRaiseAccumulateApi
public suspend inline fun <reified R : Any> RoutingCall.receiveAccumulating(): RaiseAccumulate.Value<R> =
  ra.accumulating { receiveOrRaise(typeInfo<R>()) }

context(ra: RaiseAccumulate<RequestError>)
@ExperimentalRaiseAccumulateApi
public suspend inline fun <reified R : Any> RoutingCall.receiveFormAccumulating(): RaiseAccumulate.Value<AccumulatingParameters> =
  ra.accumulating { AccumulatingParameters(formParameters()) }
