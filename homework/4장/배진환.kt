import kotlin.math.pow
import fpkotlin.List.Cons
import fpkotlin.List.Nil
import fpkotlin.List

sealed class Option<out A : Any>
data class Some<out A : Any>(val get: A) : Option<A>()
object None : Option<Nothing>()

// 1.
fun <A : Any, B : Any> Option<A>.map(f: (A) -> B): Option<B> = when (this) {
    is None -> None
    is Some -> Some(f(this.get))
}

fun <A : Any> Option<A>.getOrElse(default: () -> A): A = when (this) {
    is None -> default()
    is Some -> get
}

fun <A : Any, B : Any> Option<A>.flatMap(f: (A) -> Option<B>): Option<B> = map(f).getOrElse { None }
fun <A : Any> Option<A>.orElse(f: () -> Option<A>): Option<A> = map(::Some).getOrElse { f() }
fun <A : Any> Option<A>.filter(f: (A) -> Boolean): Option<A> =
    flatMap { if (f(it)) Some(it) else None }

//2.
fun mean(l: List<Double>): Option<Double> =
    if (l.isEmpty()) None else Some(l.sum() / l.size())

fun variance(l: List<Double>): Option<Double> =
    mean(l).flatMap { m ->
        mean(l.map { x ->
            (x - m).pow(2)
        })
    }

//3.
fun <A : Any, B : Any, C : Any> map2(
    oa: Option<A>,
    ob: Option<B>,
    f: (A, B) -> C
): Option<C> = oa.flatMap { a ->
    ob.map { b ->
        f(a, b)
    }
}

//4
fun <A : Any> sequence(l: List<Option<A>>): Option<List<A>> =
    l.foldRight(Some(Nil)) { oa1: Option<A>, oa2: Option<List<A>> ->
        map2(oa1, oa2) { a1: A, a2: List<A> -> Cons<A>(a1, a2) }
    }


//5
fun <A : Any, B : Any> traverse(
    l: List<A>,
    f: (A) -> Option<B>
): Option<List<B>> =
    when (l) {
        is Nil -> Some(Nil)
        is Cons -> map2(f(l.head), traverse(l.tail, f)) { b, l ->
            Cons(b, l)
        }
    }


/**
fun <A : Any> sequence(l: List<Option<A>>): Option<List<A>> = traverse(l){ it }
 **/


// 6

// 4.6
sealed class Either<out E:Any, out A:Any>

data class Left<out E>(val value: E) : Either<E, Nothing>()
data class Right<out A>(val value: A) : Either<Nothing, A>()

fun <E : Any, A: Any, B:Any> Either<E, A>.map(f: (A) -> B): Either<E, B> =
    when (this) {
        is Right -> Right(f(this.value))
        is Left -> this
    }

fun <E : Any, A : Any, B : Any> Either<E, A>.flatMap(f: (A) -> Either<E, B>): Either<E, B> =
    when (this) {
        is Right -> f(this.value)
        is Left -> this
    }

fun <E : Any, A : Any, B> Either<E, A>.orElse(f: () -> Either<E, A>): Either<E, A> =
    when (this) {
        is Right -> this
        is Left -> f()
    }


fun <E : Any, A : Any, B : Any, C : Any> map2(
    ae: Either<E, A>,
    be: Either<E, B>,
    f: (A, B) -> C
): Either<E, C> =
    ae.flatMap { a ->
        be.map { b ->
            f(a, b)
        }
    }

// 4.7
fun <E : Any, A, B : Any> traverse(
    l: List<A>,
    f: (A) -> Either<E, B>
): Either<E, List<B>> =
    when (l) {
        is Nil -> Right(Nil)
        is Cons ->
            map2(f(l.head), traverse(l.tail, f)) { b, l2 ->
                Cons(b, l2)
            }
    }

// 4.8
sealed class Partial<out A, out B>

data class Failures<out A>(val get: List<A>) : Partial<A, Nothing>()
data class Success<out B>(val get: B) : Partial<Nothing, B>()


