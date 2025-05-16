package arrow.raise.ktor.server.request

import arrow.core.NonEmptyList
import arrow.core.raise.ExperimentalRaiseAccumulateApi
import arrow.core.raise.Raise
import arrow.core.raise.RaiseAccumulate
import arrow.core.raise.accumulate
import arrow.core.raise.withError
import arrow.raise.ktor.server.Response
import arrow.raise.ktor.server.errorsResponse
import io.ktor.server.routing.*

@ExperimentalRaiseAccumulateApi
context(r: Raise<Response>, ctx: RoutingContext)
public inline fun <R> validate(
  transform: (NonEmptyList<RequestError>) -> Response = ctx.call::errorsResponse,
  block: context(RoutingContext) RaiseAccumulate<RequestError>.() -> R,
): R = r.withError(transform) { accumulate { block() } }
