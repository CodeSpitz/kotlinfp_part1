// 연습문제 4.1
fun <A, B> Option<A>.map(f: (A) -> B): Option<B> =
    when (this) {
        is None -> None
        is Some -> Some(f(this.get))
    }

fun <A> Option<A>.getOrElse(default: () -> A): A =
    when (this) {
        is None -> default()
        is Some -> this.get
    }

fun <A, B> Option<A>.flatMap(f: (A) -> Option<B>): Option<B> =
    this.map(f).getOrElse { None }

fun <A> Option<A>.orElse(ob: () -> Option<A>): Option<A> =
    this.map { Some(it) }.getOrElse { ob() }

fun <A> Option<A>.filter(f: (A) -> Boolean): Option<A> =
    this.flatMap { a -> if (f(a)) Some(a) else None }

// 연습문제 4.2
fun mean(xs: List<Double>): Option<Double> =
    if (xs.isEmpty()) None
    else Some(xs.sum() / xs.size())

fun variance(xs: List<Double>): Option<Double> =
    mean(xs).flatMap { m ->
        mean(xs.map { x->
            (x - m).pow(2)
        })
    }

// 연습문제 4.3
fun <A, B, C> map2(
    oa: Option<A>,
    ob: Option<B>,
    f: (A, B) -> C
): Option<C> =
    oa.flatMap { a ->
        ob.map { b ->
            f(a, b)
        }
    }

// 연습문제 4.4
fun <A> sequence(
    xs: List<Option<A>>
): Option<List<A>> =
    xs.foldRight(Some(Nil),
        { oa1: Option<A>, oa2: Option<List<A>> ->
            map2(oa1, oa2) { a1: A, a2: List<A> ->
                Cons(a1, a2)
            }
        })

// 연습문제 4.5
tailrec fun <A, B> traverse(
    xs: List<A>,
    f: (A) -> Option<B>
): Option<List<B>> =
    when (xa) {
        is Nil -> Some(Nil)
        is Cons ->
            map2(f(xa.head), traverse(xa.tail, f)) { b, xb ->
                Cons(b, xb)
            }
    }

fun <A> sequence(xs: List<Option<A>>): Option<List<A>> =
    traverse(xs) { it }

// 연습문제 4.6
fun <E, A, B> Either<E, A>.map(f: (A) -> B): Either<E, B> =
    when (this) {
        is Left -> this
        is Right -> Right(f(this.value))
    }

fun <E, A> Eigher<E, A>.orElse(f: () -> Either<E, A>): Either<E, A> =
    when (this) {
        is Left -> f()
        is Right -> this
    }

fun <E, A, B, C> map2(
    ae: Either<E, A>,
    be: Either<E, B>,
    f: (A, B) -> C
): Either<E, C> =
    ae.flatMap { a -> be.map { b -> f(a, b) } }

// 연습문제 4.7
fun <E, A, B> traverse(
    xs: List<A>,
    f: (A) -> Either<E, B>
): Either<E, List<B>> =
    when (xs) {
        is Nil -> Right(Nil)
        is Cons ->
            map2(f(xs.head), traverse(xs.tail, f)) { b, xb ->
                Cons(b, xb)
            }
    }

fun <E, A> sequence(es: List<Either<E, A>>): Either<E, List<A>> =
    traverse(es) { it }

// 연습문제 4.8
sealed class Partial<out A, out B>

data class Failures<out A>(val get: List<A>): Partial<A, Nothing>()
data class Success<out B>(val get: B): Partial<Nothing, B>()