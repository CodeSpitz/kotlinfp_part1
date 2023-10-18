package codespitz_study_kotlinfunctionalprogramming.`4장`

import arrow.core.Either
import arrow.core.Either.Right
import arrow.core.Either.Left
import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.doubles.plusOrMinus
import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import io.kotest.matchers.shouldBe
import kotlin.math.pow

// Exercise 4.1
fun <A, B> Option<A>.map(f: (A) -> B): Option<B> =
    when (this) {
        is Some -> Some(f(value))
        None -> None
    }

fun <A, B> Option<A>.flatMap(f: (A) -> Option<B>): Option<B> =
    when (this) {
        is Some -> f(value)
        None -> None
    }

fun <A> Option<A>.getOrElse(default: () -> A): A =
    when (this) {
        is Some -> value
        None -> default()
    }

fun <A> Option<A>.orElse(ob: () -> Option<A>): Option<A> =
    when (this) {
        is Some -> this
        None -> ob()
    }

fun <A> Option<A>.filter(f: (A) -> Boolean): Option<A> =
    when (this) {
        is Some -> if (f(value)) this else None
        None -> None
    }

fun <A, B> Option<A>.flatMap_2(
    f: (A) -> Option<B>
): Option<B> =
    when (this) {
        is Some -> f(value)
        None -> None
    }

fun <A> Option<A>.orElse_2(
    ob: () -> Option<A>
): Option<A> =
    when (this) {
        is Some -> this
        None -> ob()
    }

fun <A> Option<A>.filter_2(
    f: (A) -> Boolean
): Option<A> =
    when (this) {
        is Some -> if (f(value)) this else None
        None -> None
    }


// Exercise 4.2
fun mean(xs: List<Double>): Option<Double> =
    if (xs.isEmpty()) None
    else Some(xs.sum() / xs.size)

fun variance(xs: List<Double>): Option<Double> =
    mean(xs).flatMap { mean ->
        val squaredDifferences = xs.map { x -> (x - mean).pow(2) }
        mean(squaredDifferences)
    }


// Exercise 4.3
fun <A, B, C> map2(a: Option<A>, b: Option<B>, f: (A, B) -> C): Option<C> =
    a.flatMap { aValue ->
        b.map { bValue ->
            f(aValue, bValue)
        }
    }

// Exercise 4.4
fun <A> sequence(xs: List<Option<A>>): Option<List<A>> =
    xs.foldRight(Some(emptyList())) { oa, acc ->
        when (oa) {
            is Some -> acc.map { list -> listOf(oa.value) + list }
            is None -> None
        }
    }

// 연습문제 Exercise 4.5
fun <A, B> traverse(
    xa: List<A>,
    f: (A) -> Option<B>
): Option<List<B>> =
    when (xa) {
        is Nil -> Some(Nil)
        is Cons -> map2(f(xa.head), traverse(xa.tail, f)) { b, list -> Cons(b, list) }
    }

fun <A> sequence2(xs: List<Option<A>>): Option<List<A>> =
    traverse(xs) { it }

fun <A> catches(a: () -> A): Option<A> =
    try {
        Some(a())
    } catch (e: Throwable) {
        None
    }


// Exercise 4.6
fun <E, A, B> Either<E, A>.map(f: (A) -> B): Either<E, B> =
    when (this) {
        is Left -> this
        is Right -> Right(f(this.value))
    }

fun <E, A, B> Either<E, A>.flatMap(f: (A) -> Either<E, B>): Either<E, B> =
    when (this) {
        is Right -> f(this.value)
        is Left -> this
    }

fun <E, A> Either<E, A>.orElse(f: () -> Either<E, A>): Either<E, A> =
    when (this) {
        is Right -> this
        is Left -> f()
    }

fun <E, A, B, C> map2(
    ae: Either<E, A>,
    be: Either<E, B>,
    f: (A, B) -> C
): Either<E, C> =
    ae.flatMap { a -> be.map { b -> f(a, b) } }


// Exercise 4.7
fun <E, A, B> traverse3(
    xs: List<A>,
    f: (A) -> Either<E, B>
): Either<E, List<B>> =
    xs.fold(Right(emptyList())) { acc, a ->
        acc.flatMap { list ->
            f(a).map { b ->
                list + b
            }
        }
    }

fun <E, A> sequence3(es: List<Either<E, A>>): Either<E, List<A>> =
    traverse3(es) { it }

fun <A> catches3(a: () -> A): Either<String, A> =
    try {
        Right(a())
    } catch (e: Exception) {
        Left(e.message ?: "An error occurred")
    }


