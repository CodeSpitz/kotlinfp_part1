sealed class Option<out A>

data class Some<out A>(val get: A) : Option<A>()

object None : Option<Nothing>()

// ex 4.1
fun <A, B> Option<A>.map(f: (A) -> B): Option<B> = when (this) {
    is None -> None
    is Some -> Some(f(this.get))
}

fun <A, B> Option<A>.flatMap(f: (A) -> Option<B>): Option<B> = when (this) {
    is None -> None
    is Some -> f(get);
}

fun <A> Option<A>.getOrElse(default: () -> A): A = when (this) {
    is None -> default()
    is Some -> this.get
}

fun <A> Option<A>.orElse(default: () -> Option<A>): Option<A> = when (this) {
    is None -> default()
    is Some -> this
}

fun <A> Option<A>.filter(f: (A) -> Boolean): Option<A> = when (this) {
    is None -> None
    is Some -> if (f(this.get)) this else None
}

fun variance(xs: List<Double>): Option<Double> =
        mean(xs).flatMap { m ->
            mean(xs.map { x -> Math.pow(x - m, 2.0) })
        }

fun <A, B, C> map2(x: Option<A>, y: Option<B>, f: (A, B) -> C): Option<C> =
        x.flatMap { a ->
            y.map { b ->
                f(a, b)
            }
        }

fun <A> sequence(xs: List<Option<A>>): Option<List<A>> =
        xs.foldRight(Some(Nil)) { x: Option<A>, y: Option<List<A>> ->
            map2(x, y) { a, b -> Cons(a, b) }
        }

fun <A, B> traverse(xa: List<A>, f: (A) -> Option<B>): Option<List<B>> =
        when (xa) {
            is Nil -> Some(Nil)
            is Cons -> map2(f(xa.head), traverse(xa.tail, f)) { a, b -> Cons(a, b) }
        }

fun <E, A, B> Either<E, A>.map(f: (A) -> B): Either<E, B> =
        when (this) {
            is Left -> this
            is Right -> Right(f(this.value))
        }

fun <E, A> Either<E, A>.orElse(f: () -> Either<E, A>): Either<E, A> =
        when (this) {
            is Left -> f()
            is Right -> this
        }

fun <E, A, B, C> map2(ae: Either<E, A>, be: Either<E, B>, f: (A, B) -> C): Either<E, C> =
        ae.flatMap { a ->
            be.map { b ->
                f(a, b)
            }
        }

fun <E, A, B> Either<E, A>.flatMap(f: (A) -> Either<E, B>): Either<E, B> = when (this) {
    is Left -> this
    is Right -> f(this.value)
}

fun <E,A,B> traverse(
        xs: List<A>,
        f: (A) -> Either<E, B>
) : Either<E, List<B>> =
        when (xs) {
            is Nil -> Right(Nil)
            is Cons ->
                map2(f(xs.head), traverse(xs.tail, f)) { b, xb ->
                    Cons(b, xb)
                }
        }


fun mean(xs: List<Double>): Option<Double> = Some(1.0)

sealed class Either<out E, out A>

data class Left<out E>(val value: E) : Either<E, Nothing>()

data class Right<out A>(val value: A) : Either<Nothing, A>()
