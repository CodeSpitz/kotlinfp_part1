package chpater4

import chapter3.*

//4.1
fun <A, B> Option<A>.map(f: (A) -> B): Option<B> = when (this) {
    is None -> None
    is Some -> Some(f(this.get))
}

fun <A, B> Option<A>.flatMap(f: (A) -> Option<B>): Option<B> = when (this) {
    is None -> None
    is Some -> f(this.get)
}

fun <A> Option<A>.getOrElse(default: () -> A): A = when (this) {
    is None -> default()
    is Some -> this.get
}

fun <A> Option<A>.orElse(ob: () -> Option<A>): Option<A> = when (this) {
    is None -> ob()
    is Some -> this
}

fun <A> Option<A>.filter(f: (A) -> Boolean): Option<A> = when (this) {
    is None -> None
    is Some -> if (f(this.get)) this else None
}

//4.2
fun variance(xs: FList<Double>): Option<Double> =
    mean(xs).flatMap { m -> mean(xs.map { x -> Math.pow(x - m, 2.0) }) }

//4.3
fun <A, B, C> map2(
    oa: Option<A>,
    ob: Option<B>,
    f: (A, B) -> C
): Option<C> = oa.flatMap { a -> ob.map { b -> f(a, b) } }

//4.4
fun <A, B> sequence(
    xs: FList<Option<A>>
): Option<FList<A>> = xs.foldRight(Some(FList.of())) { a, acc ->
    map2(a, acc) { b, list -> Cons(b, list) }
}

//4.5
fun<A,B> traverse(
    xa: FList<A>,
    f: (A) -> Option<B>
): Option<FList<B>> = xa.foldRight(Some(FList.of())) { a, acc ->
    map2(f(a), acc) { b, list -> Cons(b, list) }
}

//4.6
fun <E, A, B> Either<E, A>.map(f: (A) -> B): Either<E, B> = when (this) {
    is Left -> this
    is Right -> Right(f(this.value))
}

fun <E, A, B> Either<E, A>.flatMap(f: (A) -> Either<E, B>): Either<E, B> = when (this) {
    is Left -> this
    is Right -> f(this.value)
}

fun <E, A, B, C> map2(
    ea: Either<E, A>,
    eb: Either<E, B>,
    f: (A, B) -> C
): Either<E, C> = ea.flatMap { a -> eb.map { b -> f(a, b) } }

//4.7
fun <E, A> sequence(
    xs: FList<Either<E, A>>
): Either<E, FList<A>> = xs.foldRight(Right(FList.of())) { a, acc ->
    map2(a, acc) { b, list -> Cons(b, list) }
}

fun <E, A> traverse(
    xs: FList<A>,
    f: (A) -> Either<E, A>
): Either<E, FList<A>> = xs.foldRight(Right(FList.of())) { a, acc ->
    map2(f(a), acc) { b, list -> Cons(b, list) }
}

//4.8
sealed class Partial<out A, out B>

data class Failures<out A>(val get: FList<A>) : Partial<A, Nothing>()
data class Success<out B>(val get: B) : Partial<Nothing, B>()

sealed class Option<out A>

data class Some<out A>(val get:A) : Option<A>()

object None : Option<Nothing>()

fun mean(xs: FList<Double>): Option<Double> =
    if (xs.isEmpty()) None
    else Some(xs.foldRight(0.0) { a, b -> a + b } / xs.length())

sealed class Either<out E, out A>
data class Left<out E>(val value: E) : Either<E, Nothing>()
data class Right<out A>(val value: A) : Either<Nothing, A>()

inline fun <A, B> FList<A>.map(crossinline f: (A) -> B): FList<B> = foldRight(this, FList.of()) { a, ls -> Cons(f(a), ls)}

fun <A, B> FList<A>.foldRight(z: B, f: (A, B) -> B): B = when (this) {
    is Nil -> z
    is Cons -> f(this.head, foldRight(this.tail, z, f))
}

fun <A> FList<A>.isEmpty(): Boolean = when (this) {
    is Nil -> true
    is Cons -> false
}

fun <A> FList<A>.length(): Int = foldRight(0) { _, b -> b + 1 }
