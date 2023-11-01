package study.fp

import study.fp.FOption.*
import study.fp.FList.*
import study.fp.FEither.*
import kotlin.math.pow

sealed class FOption<out A> {
    data class Some<out A>(val get: A): FOption<A>()
    object None: FOption<Nothing>()
}

sealed class FEither<out E, out A> {
    data class Left<out E>(val value: E): FEither<E, Nothing>()
    data class Right<out A>(val value: A): FEither<Nothing, A>()
}

/* 연습문제 4.1 */
fun <P1, RETURN> FOption<P1>.map(f: (P1) -> RETURN): FOption<RETURN> =
    when (this) {
        is None -> None
        is Some -> Some(f(this.get))
    }

fun <RETURN> FOption<RETURN>.getOrElse(f: () -> RETURN): RETURN =
    when (this) {
        is None -> f()
        is Some -> this.get
    }

fun <P1, RETURN> FOption<P1>.flatMap(f: (P1) -> FOption<RETURN>): FOption<RETURN> =
    when (this) {
        is None -> None
        is Some -> f(this.get)
    }

fun <RETURN> FOption<RETURN>.orElse(f: () -> FOption<RETURN>): FOption<RETURN> =
    when (this) {
        is None -> f()
        is Some -> this
    }

fun <RETURN> FOption<RETURN>.filter(f: (RETURN) -> Boolean): FOption<RETURN> =
    when (this) {
        is None -> None
        is Some -> if (f(this.get)) this else None
    }

/* 연습문제 4.2 */
fun mean(list: FList<Double>): FOption<Double> =
    if (list.isEmpty()) None else Some(list.sum() / list.size)

fun variance(list: FList<Double>): FOption<Double> =
    mean(list).flatMap { m ->
        mean(list.map { x ->
            (x - m).pow(2)
        })
    }

/* 연습문제 4.3 */
fun <P1, P2, RETURN> map2(o1: FOption<P1>, o2: FOption<P2>, f: (P1, P2) -> RETURN): FOption<RETURN> =
    o1.flatMap { a -> o2.map { b -> f(a, b) } }

/* 연습문제 4.4 */
fun <RETURN> sequence(list: FList<FOption<RETURN>>): FOption<FList<RETURN>> =
    list.foldRight(Some(Nil)) { o1: FOption<RETURN>, o2: FOption<FList<RETURN>> ->
        map2(o1, o2) { a1: RETURN, a2: FList<RETURN> ->
            Cons(a1, a2)
        }
    }

/* 연습문제 4.5 */
fun <P1, RETURN> traverse(list: FList<P1>, f: (P1) -> FOption<RETURN>): FOption<FList<RETURN>> =
    when (list) {
        is Nil -> Some(Nil)
        is Cons -> map2(f(list.head), traverse(list.tail, f)) { a, b ->
            Cons(a, b)
        }
    }

fun <RETURN> sequence2(list: FList<FOption<RETURN>>): FOption<FList<RETURN>> =
    traverse(list) { it }

/* 연습문제 4.6 */
fun <E, P1, RETURN> FEither<E, P1>.map(f: (P1) -> RETURN): FEither<E, RETURN> =
    when (this) {
        is Left -> this
        is Right -> Right(f(this.value))
    }

fun <E, P1, RETURN> FEither<E, P1>.flatmap(f: (P1) -> FEither<E, RETURN>): FEither<E, RETURN> =
    when (this) {
        is Left -> this
        is Right -> f(this.value)
    }

fun <E, RETURN> FEither<E, RETURN>.orElse(f: () -> FEither<E, RETURN>): FEither<E, RETURN> =
    when (this) {
        is Left -> f()
        is Right -> this
    }

fun <E, P1, P2, RETURN> map2ByEither(
    ae: FEither<E, P1>,
    be: FEither<E, P2>,
    f: (P1, P2) -> RETURN)
: FEither<E, RETURN> =
    ae.flatmap { a -> be.map { b -> f(a,b) } }

/* 연습문제 4.7 */
fun <E, P1, RETURN> traverseByEither(list: FList<P1>, f: (P1) -> FEither<E, RETURN>): FEither<E, FList<RETURN>> =
    when (list) {
        is Nil -> Right(Nil)
        is Cons -> map2ByEither(f(list.head), traverseByEither(list.tail, f)) { b, xb ->
            Cons(b, xb)
        }
    }

fun <E, RETURN> sequenceByEither(list: FList<FEither<E, RETURN>>): FEither<E, FList<RETURN>> =
    traverseByEither(list) { it }

/*
 * 연습문제 4.8
 * 오류 리스트를 유지하게 해주는 새 데이터 타입을 사용하면 해결할 수 있다.
 */


