import Option.*
import Either.*
import FList.*
import kotlin.math.pow

sealed class Option<out A> {
    data class Some<out A>(val get: A): Option<A>()
    object None: Option<Nothing>()
}

sealed class FList<out A> {
    data class Cons<out A>(val head: A, val tail: FList<A>): FList<A>()
    object Nil: FList<Nothing>()
}

private fun <A, RETURN> FList<A>.foldRight(
    some: Some<Nil>,
    function: (A, Option<FList<RETURN>>) -> Option<Cons<RETURN>>,
): Option<FList<RETURN>> {
    TODO("Not yet implemented")
}

sealed class Either<out E, out A> {
    data class Left<out E>(val value: E): Either<E, Nothing>()
    data class Right<out A>(val value: A): Either<Nothing, A>()
}

/* 연습문제 4.1 */
fun <P1, RETURN> Option<P1>.map(f: (P1) -> RETURN): Option<RETURN> =
    when (this) {
        is None -> None
        is Some -> Some(f(this.get))
    }

fun <RETURN> Option<RETURN>.getOrElse(f: () -> RETURN): RETURN =
    when (this) {
        is None -> f()
        is Some -> this.get
    }

fun <P1, RETURN> Option<P1>.flatMap(f: (P1) -> Option<RETURN>): Option<RETURN> =
    when (this) {
        is None -> None
        is Some -> f(this.get)
    }

fun <RETURN> Option<RETURN>.orElse(f: () -> Option<RETURN>): Option<RETURN> =
    when (this) {
        is None -> f()
        is Some -> this
    }

fun <RETURN> Option<RETURN>.filter(f: (RETURN) -> Boolean): Option<RETURN> =
    when (this) {
        is None -> None
        is Some -> if (f(this.get)) this else None
    }

/* 연습문제 4.2 */
fun mean(list: List<Double>): Option<Double> =
    if (list.isEmpty()) None else Some(list.sum() / list.size)

fun variance(list: List<Double>): Option<Double> =
    mean(list).flatMap { m ->
        mean(list.map { x ->
            (x - m).pow(2)
        })
    }

/* 연습문제 4.3 */
fun <P1, P2, RETURN> map2(o1: Option<P1>, o2: Option<P2>, f: (P1, P2) -> RETURN): Option<RETURN> =
    o1.flatMap { a -> o2.map { b -> f(a, b) } }

/* 연습문제 4.4 */
fun <RETURN> sequence(list: FList<Option<RETURN>>): Option<FList<RETURN>> =
    list.foldRight(Some(Nil)) { o1: Option<RETURN>, o2: Option<FList<RETURN>> ->
        map2(o1, o2) { a1: RETURN, a2: FList<RETURN> ->
            Cons(a1, a2)
        }
    }

/* 연습문제 4.5 */
fun <P1, RETURN> traverse(list: FList<P1>, f: (P1) -> Option<RETURN>): Option<FList<RETURN>> =
    when (list) {
        is Nil -> Some(Nil)
        is Cons -> map2(f(list.head), traverse(list.tail, f)) { a, b ->
            Cons(a, b)
        }
    }

fun <RETURN> sequence2(list: FList<Option<RETURN>>): Option<FList<RETURN>> =
    traverse(list) { it }

/* 연습문제 4.6 */
fun <E, P1, RETURN> Either<E, P1>.map(f: (P1) -> RETURN): Either<E, RETURN> =
    when (this) {
        is Left -> this
        is Right -> Right(f(this.value))
    }

fun <E, P1, RETURN> Either<E, P1>.flatmap(f: (P1) -> Either<E, RETURN>): Either<E, RETURN> =
    when (this) {
        is Left -> this
        is Right -> f(this.value)
    }

fun <E, RETURN> Either<E, RETURN>.orElse(f: () -> Either<E, RETURN>): Either<E, RETURN> =
    when (this) {
        is Left -> f()
        is Right -> this
    }

fun <E, P1, P2, RETURN> map2ByEither(
    ae: Either<E, P1>,
    be: Either<E, P2>,
    f: (P1, P2) -> RETURN)
: Either<E, RETURN> =
    ae.flatmap { a -> be.map { b -> f(a,b) } }

/* 연습문제 4.7 */
fun <E, P1, RETURN> traverseByEither(list: FList<P1>, f: (P1) -> Either<E, RETURN>): Either<E, FList<RETURN>> =
    when (list) {
        is Nil -> Right(Nil)
        is Cons -> map2ByEither(f(list.head), traverseByEither(list.tail, f)) { b, xb ->
            Cons(b, xb)
        }
    }

fun <E, RETURN> sequenceByEither(list: FList<Either<E, RETURN>>): Either<E, FList<RETURN>> =
    traverseByEither(list) { it }

/*
 * 연습문제 4.8
 * 오류 리스트를 유지하게 해주는 새 데이터 타입을 사용하면 해결할 수 있다.
 */


