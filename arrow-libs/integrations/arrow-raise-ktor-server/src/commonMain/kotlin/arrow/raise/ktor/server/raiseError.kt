package arrow.raise.ktor.server

import arrow.core.NonEmptyList
import arrow.core.raise.Raise
import arrow.core.raise.withError
import arrow.raise.ktor.server.request.RequestError
import io.ktor.server.routing.RoutingContext

context(r: Raise<Response>)
@PublishedApi
internal inline fun <R> RoutingContext.raiseError(f: Raise<RequestError>.() -> R): R = r.withError(call::errorResponse, f)

context(r: Raise<Response>)
@PublishedApi
internal inline fun <R> RoutingContext.raiseErrors(f: Raise<NonEmptyList<RequestError>>.() -> R): R = r.withError(call::errorsResponse, f)
