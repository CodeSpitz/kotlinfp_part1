import kotlin.math.pow

sealed class Option<out A> {
    object None : Option<Nothing>()
    data class Some<out A>(val get: A) : Option<A>()

    companion object {
        fun <A> of(value: A): Option<A> =
            Some(value)
    }
}


fun <A, B> Option<A>.flatMap(f: (A) -> Option<B>): Option<B> =
    when (this) {
        is Option.None -> Option.None
        is Option.Some -> f(this.get)
    }

fun <A, B> Option<A>.map(f: (A) -> B): Option<B> =
    this.flatMap { a -> Option.of(f(a)) }

fun <A> Option<A>.getOrElse(default: () -> A): A =
    when (this) {
        is Option.None -> default.invoke()
        is Option.Some -> this.get
    }

fun <A> Option<A>.orElse(ob: () -> Option<A>): Option<A> =
    when (this) {
        is Option.None -> ob.invoke()
        is Option.Some -> this
    }

fun <A> Option<A>.filter(f: (A) -> Boolean): Option<A> =
    when (this) {
        is Option.None -> Option.None
        is Option.Some -> {
            if (f(this.get)) this
            else Option.None
        }
    }

// 4.2

fun mean(list: List<Double>): Option<Double> =
    if (list.isEmpty()) Option.None
    else Option.of(list.sum() / list.size)

fun variance(list: List<Double>): Option<Double> =
    mean(list).flatMap { m ->
        mean(list.map { (it - m).pow(2) })
    }

// 4.3
fun <A, B, C> map2(a: Option<A>, b: Option<B>, f: (A, B) -> C): Option<C> =
    a.flatMap { aValue ->
        b.map { f(aValue, it) }
    }

// 4.4
fun <A> sequence(list: List<Option<A>>): Option<List<A>> =
    list.foldRight(Option.Some(Nil)) { oa1: Option<A>, oa2: Option<List<A>> ->
        map2(oa1, oa2) { a1: A, a2: List<A> ->
            Cons(a1, a2)
        }
    }

// 4.5
fun <A, B> traverse(list: List<A>, f: (A) -> Option<B>): Option<List<B>> =
    when (list) {
        is Nil -> Option.Some(Nil)
        is Cons -> map2(f(list.head), traverse(list.tail, f)) { b, xb ->
            Cons(b, xb)
        }
    }

// 4.6
sealed class Either<out E, out A> {
    data class Left<out E>(val error: E) : Either<E, Nothing>()
    data class Right<out A>(val value: A) : Either<Nothing, A>()

    companion object {
        fun <A> of(value: A): Either<Nothing, A> =
            Right(value)
    }
}

fun <E, A, B> Either<E, A>.map(f: (A) -> B): Either<E, B> =
    when (this) {
        is Either.Left -> this
        is Either.Right -> Either.of(f(this.value))
    }

fun <E, A, B> Either<E, A>.flatMap(f: (A) -> Either<E, B>): Either<E, B> =
    when (this) {
        is Either.Left -> this
        is Either.Right -> f(this.value)
    }

fun <E, A> Either<E, A>.orElse(f: () -> Either<E, A>): Either<E, A> =
    when (this) {
        is Either.Left -> f.invoke()
        is Either.Right -> this
    }

fun <E, A, B, C> map2(ae: Either<E, A>, be: Either<E, B>, f: (A, B) -> C): Either<E, C> =
    ae.flatMap { aValue ->
        be.map { f(aValue, it) }
    }

// 4.7
fun <E, A, B> traverse(list: List<A>, f: (A) -> Either<E, B>): Either<E, List<B>> =
    when(list) {
        is Nil -> Right(Nil)
        is Cons -> map2(f(list.head), traverse(list.tail, f)) { b, xb ->
            Cons(b, xb)
        }
    }

fun <E, A> sequence(list: List<Either<E, A>>): Either<E, List<A>> =
    traverse(list) { it }

// 4.8
Either의 E 부분 타입을 누적시킬 수 있도록 컬렉션 타입을 사용한다
