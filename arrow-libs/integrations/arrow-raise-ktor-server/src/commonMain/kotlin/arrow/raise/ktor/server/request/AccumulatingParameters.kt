package arrow.raise.ktor.server.request

import arrow.core.raise.ExperimentalRaiseAccumulateApi
import arrow.core.raise.RaiseAccumulate
import io.ktor.http.Parameters
import io.ktor.server.routing.*
import kotlin.jvm.JvmInline
import kotlin.jvm.JvmName

@JvmInline
public value class AccumulatingParameters(@PublishedApi internal val parameters: RaisingParameters): Parameters by parameters {
  context(r: RaiseAccumulate<RequestError>)
  @ExperimentalRaiseAccumulateApi
  public operator fun invoke(name: String): RaiseAccumulate.Value<String> =
    r.accumulating { parameters(name) }

  context(r: RaiseAccumulate<RequestError>)
  @ExperimentalRaiseAccumulateApi
  public inline operator fun <reified T : Any> invoke(name: String, marker: Unit = Unit): RaiseAccumulate.Value<T> =
    r.accumulating { parameters<T>(name) }

  context(r: RaiseAccumulate<RequestError>)
  @ExperimentalRaiseAccumulateApi
  public inline operator fun <T : Any> invoke(name: String, transform: ParameterTransform<T>): RaiseAccumulate.Value<T> =
    r.accumulating { parameters(name,transform) }

  context(_: RaiseAccumulate<RequestError>)
  @ExperimentalRaiseAccumulateApi
  public inline operator fun <reified T: Any> get(name: String): RaiseAccumulate.Value<T> = invoke<T>(name)

  context(r: RaiseAccumulate<RequestError>)
  @ExperimentalRaiseAccumulateApi
  public operator fun invoke(): AccumulatingProvider<String> =
    provider { invoke(it) }

  context(r: RaiseAccumulate<RequestError>)
  @ExperimentalRaiseAccumulateApi
  public inline operator fun <reified T : Any> invoke(marker: Unit = Unit): AccumulatingProvider<T> =
    provider { invoke<T>(it) }

  context(r: RaiseAccumulate<RequestError>)
  @ExperimentalRaiseAccumulateApi
  public inline operator fun <T : Any> invoke(crossinline transform: ParameterTransform<T>): AccumulatingProvider<T> =
    provider { invoke(it, transform) }
}

context(ctx: RoutingContext)
public inline val pathAccumulating: AccumulatingParameters get() = ctx.call.pathAccumulating

context(ctx: RoutingContext)
public inline val queryAccumulating: AccumulatingParameters get() = ctx.call.queryAccumulating

public inline val RoutingCall.pathAccumulating: AccumulatingParameters get() = AccumulatingParameters(pathRaising)

public inline val RoutingCall.queryAccumulating: AccumulatingParameters get() = AccumulatingParameters(queryRaising)
