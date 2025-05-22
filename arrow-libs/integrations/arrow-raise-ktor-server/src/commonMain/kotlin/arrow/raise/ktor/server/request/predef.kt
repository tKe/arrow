package arrow.raise.ktor.server.request

import arrow.core.raise.RaiseAccumulate
import io.ktor.util.reflect.*
import kotlin.jvm.JvmInline
import kotlin.properties.PropertyDelegateProvider
import kotlin.reflect.KProperty

internal inline val TypeInfo.simpleName get(): String = type.simpleName ?: type.toString()

internal typealias Provider<T> = PropertyDelegateProvider<Nothing?, T>
internal typealias AccumulatingProvider<T> = Provider<RaiseAccumulate.Value<T>>
internal typealias RaisingProvider<T> = Provider<Value<T>>

@PublishedApi
internal inline fun <T> provider(crossinline f: (String) -> T): Provider<T> = Provider { _, p -> f(p.name) }

@JvmInline
public value class Value<T>(public val value: T) {
  @Suppress("NOTHING_TO_INLINE")
  public inline operator fun getValue(thisRef: Any?, property: KProperty<*>): T = value
}
