package study.fp

import kotlin.math.pow

sealed class Option<out A> {


    companion object {
        inline operator fun invoke() = None
        inline operator fun <A> invoke(data: A) = Some(data)
    }

}

data class Some<out A> constructor(val data:A): Option<A>()

object None: Option<Nothing>()


// 4.1
fun <A, B> Option<A>.map(f: (A) -> B): Option<B> = when(this) {
    is None -> Option()
    is Some -> Option(f(data))
}

fun <A, B> Option<A>.flatMap(f: (A) -> Option<B>): Option<B> = when(this) {
    None -> Option()
    is Some -> f(data)
}

fun <A> Option<A>.getOrElse(default: () -> A): A = when(this) {
    None -> default()
    is Some -> data
}

fun <A> Option<A>.orElse(ob: () -> Option<A>): Option<A> = when(this) {
    None -> ob()
    is Some -> this
}

fun <A> Option<A>.filter(f: (A) -> Boolean): Option<A> = when(this) {
    None -> this
    is Some -> if (f(data)) this else Option()
}

fun mean(xs: List<Double>, onEmpty: Double): Double {
    return if (xs.isEmpty()) onEmpty else xs.sum() / xs.count()
}

// 4.2
fun variance(xs: List<Double>): Option<Double> {
    return Option(xs).flatMap { list ->
        Option(list.map { (it - mean(list, 0.0)).pow(2) })
    }.flatMap { Option(mean(it, 0.0)) }
}

// 4.3
fun <A,B,C> map2(a: Option<A>, b: Option<B>, f: (A, B) -> C): Option<C> {
    return when {
        a is Some<A> && b is Some<B> -> Option(f(a.data, b.data))
        else -> Option()
    }
}

// 4.4
fun <A : Any> ListE<Option<A>>.sequence(): Option<ListE<A>> = foldRight<Option<A>, ListE<A>>(this, Nil) { item, acc ->
    when(item){
        is Some<A> -> Cons(item.data, acc)
        None -> Nil
    }
}.let {
    if (lengthFoldLeft(it) == 0) None
    else Option(it)
}


// 4.5
fun <A, B: Any> traverse(
    xa: ListE<A>,
    f: (A) -> Option<B>
): Option<ListE<B>> = map(xa, f).sequence()



sealed class Either<out E, out A>
data class Left<out E>(val value: E): Either<E, Nothing>()
data class Right<out A>(val value: A): Either<Nothing, A>()

fun <E, A> catches(a: () -> A): Either<E, A> = try {
    Right(a())
} catch (e: Exception) {
    Left(e) as Either<E, A>
}

// 4.6
fun <E, A, B> Either<E, A>.map(f: (A) -> B): Either<E, B> = when(this) {
    is Left -> this
    is Right -> catches { f(this.value) }
}

fun <E, A, B> Either<E, A>.flatMap(f: (A) -> Either<E, B>): Either<E, B> = when(this) {
    is Left -> this
    is Right -> f(this.value)
}


fun <E, A> Either<E, A>.orElse(f: () -> Either<E, A>): Either<E, A> = when(this) {
    is Left -> f()
    is Right -> this
}

fun <E, A, B, C> map2(
    ae: Either<E,A>,
    be: Either<E, B>,
    f: (A, B) -> C
): Either<E, C> = ae.flatMap { a ->
    be.map { b -> f(a, b) }
}

// 4.7

fun <E, A, B> traverse(
    xs: ListE<A>,
    f: (A) -> Either<E, B>
): Either<E, ListE<B>> =
    when (xs) {
        is Nil -> Right(Nil)
        is Cons -> map2(f(xs.head), traverse(xs.tail, f)) { b, xb ->
            Cons(b, xb)
    }
}
fun <E, A> sequence(es: ListE<Either<E, A>>): Either<E, ListE<A>> = traverse(es) { it }

// 4.8
// Either를 그대로 활용하되, Either의 Left Data Type을 List<Exception>으로 활용할 수 있지않을까? 
















