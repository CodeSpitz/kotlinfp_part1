import Option.None
import Option.Some
import Either.Left
import Either.Right
import kotlin.math.pow

sealed class Option<out VALUE: Any> {
    data class Some<out VALUE: Any>@PublishedApi internal constructor(@PublishedApi internal val value : VALUE) : Option<VALUE>();
    data object None : Option<Nothing>();

    companion object {
        inline operator fun <VALUE: Any> invoke(value: VALUE): Option<VALUE> = Some(value)
        inline operator fun <VALUE: Any> invoke(): Option<VALUE> = None
    }
}

fun mean(xs: List<Double>): Option<Double> =
    if (xs.isEmpty()) Option<Double>()
    else Option(xs.sum() / xs.size)

fun variance(xs: List<Double>): Option<Double> = mean(xs).flatmap {
        m -> mean(xs.map { x -> (x-m).pow(2) })
}

fun <VALUE: Any, OTHER: Any> Option<VALUE>.map(f: (VALUE) -> (OTHER)): Option<OTHER> =
    when(this) {
        is Some -> Option(f(value))
        is None -> Option()
    }

fun <VALUE: Any, OTHER: Any> Option<VALUE>.flatmap(f: (VALUE) -> Option<OTHER>): Option<OTHER> =
    when(this) {
        is Some -> f(value)
        is None -> Option()
    }

fun <VALUE: Any> Option<VALUE>.getOrElse(default: () -> VALUE): VALUE =
    when(this) {
        is Some -> value
        is None -> default()
    }

fun <VALUE: Any> Option<VALUE>.orElse(ob: () -> Option<VALUE>): Option<VALUE> =
    when(this) {
        is Some -> this
        is None -> ob()
    }

fun <VALUE: Any> Option<VALUE>.filter(f: (VALUE) -> Boolean): Option<VALUE> =
    when(this) {
        is Some -> when {
            f(value) -> this
            else -> None
        }
        is None -> None
    }

fun <VALUE: Any, OTHER: Any> lift(f: (VALUE) -> OTHER): (Option<VALUE>) -> Option<OTHER> =
    { oa -> oa.map(f) }

val abs0: (Option<Double>) -> Option<Double> =
    lift { kotlin.math.abs(it) }

fun insuranceRateQuote(
    age: Int,
    numberOfSpeedingTicket: Int
): Double = TODO()

fun parseInsuranceRateQuote(
    age: String,
    speedingTickets: String
): Option<Double> {
    val optAge: Option<Int> = catches { age.toInt() }

    val optTickets: Option<Int> =
        catches { speedingTickets.toInt() }

    return map2(optAge, optTickets) { a, t -> insuranceRateQuote(a, t) }
}

fun <VALUE: Any> catches(a: () -> VALUE): Option<VALUE> =
    try {
        Some(a())
    } catch (e: Throwable) {
        None
    }

fun <VALUE: Any, OTHER: Any, RESULT: Any> map2(oa: Option<VALUE>, ob: Option<OTHER>, f: (VALUE, OTHER) -> RESULT) : Option<RESULT> =
    oa.flatmap { a -> ob.map { b -> f(a, b) } }

fun <VALUE: Any> sequence(xs: List<Option<VALUE>>): Option<List<VALUE>> =
    xs.foldRight(xs.first().map { listOf(it) }) { oit, oacc ->  map2(oit, oacc) { it, acc -> acc + listOf(it) } }

fun <VALUE: Any, OTHER: Any> traverse(
    xa: List<VALUE>,
    f: (VALUE) -> Option<OTHER>
): Option<List<OTHER>> =
    when(xa) {
        is Nil -> Some(Nil)
        is Cons ->
            map2(f(xa.head), traverse((xa.tail, f)) { b, xb ->
                Cons(b, xb)
            }
    }

fun <VALUE: Any> sequence2(xs: List<Option<VALUE>>): Option<List<VALUE>> =
    traverse(xs) { it }

sealed class Either<out ERROR: Any, out RESULT: Any> {
    data class Left<out ERROR: Any>(val value: ERROR) : Either<ERROR, Nothing>()
    data class Right<out RESULT: Any>(val value: RESULT): Either<Nothing, RESULT>()
}

fun <ERROR: Any, A: Any, B: Any> Either<ERROR, A>.map(f: (A) -> B): Either<ERROR, B> =
    when (this) {
        is Left -> this
        is Right -> Right(f(this.value))
    }

fun <ERROR: Any, A: Any, B: Any> Either<ERROR, A>.flatmap(f: (A) -> Either<ERROR, B>): Either<ERROR, B> =
    when (this) {
        is Left -> this
        is Right -> f(this.value)
    }

fun <ERROR: Any, A: Any> Either<ERROR, A>.orElse(f: () -> Either<ERROR, A>): Either<ERROR, A> =
    when (this) {
        is Left -> f()
        is Right -> this
    }

fun <ERROR: Any, A: Any, B: Any, C: Any> map2(
    ea: Either<ERROR, A>,
    eb: Either<ERROR, B>,
    f: (A, B) -> C
): Either<ERROR, C> =
    ea.flatmap { a -> eb.map { b -> f(a, b)} }

fun <ERROR: Any, A: Any, B: Any> traverse2(
    xs: List<A>,
    f: (A) -> Either<ERROR, B>
): Either<ERROR, List<B>> =
    when(xs) {
        is Nil -> Right(Nil)
        is Cons ->
            map2(f(xs.head), traverse(xs.tail, f)) { b, xb ->
                Cons(b, xb)
            }
    }

fun <ERROR: Any, A: Any> sequence(es: List<Either<ERROR, A>>): Either<ERROR, List<A>> =
    traverse2(es) { it }

sealed class Partial<out A: Any, out B: Any> {
    data class Failures<out A: Any>(val get: List<A>) : Partial<A, Nothing>()
    data class Success<out B: Any>(val get: B): Partial<Nothing, B>()
}