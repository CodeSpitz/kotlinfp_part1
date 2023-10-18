import kotlin.math.pow

// 4.1
fun <ITEM, RESULT> Option<ITEM>.map(fn: (ITEM) -> RESULT): Option<RESULT> =
    when(this) {
        is None -> None
        is Some -> Some(fn(this.get))
    }

fun <ITEM> Option<ITEM>.getOrElse(default: () -> ITEM): ITEM =
    when(this) {
        is None -> default()
        is Some -> this.get
    }

fun <ITEM, RESULT> Option<ITEM>.flatMap(fn: (ITEM) -> Option<RESULT>): Option<RESULT> =
    this.map(fn).getOrElse { None }

fun <ITEM> Option<ITEM>.orElse(ob: () -> Option<ITEM>): Option<ITEM> =
    this.map { Some(it) }.getOrElse { ob() }

fun <ITEM> Option<ITEM>.filter(fn: (ITEM) -> Boolean): Option<ITEM> =
    this.flatMap { item -> if (fn(item)) Some(item) else None }

// 4.2
fun mean(xs: List<Double>): Option<Double> =
    if (xs.isEmpty()) None
    else Some(xs.sum() / xs.size)

fun variance(xs: List<Double>): Option<Double> =
    mean(xs).flatMap { m ->
        mean(xs.map { x ->
            (x - m).pow(2)
        })
    }

// 4.3
fun <ITEM, ACC, RESULT> map2(o1: Option<ITEM>, o2: Option<ACC>, fn: (ITEM, ACC) -> RESULT): Option<RESULT> =
    o1.flatMap { item ->
        o2.map { acc ->
            fn(item, acc)
        }
    }

// 4.4
fun <ITEM> sequence(xs: List<Option<ITEM>>): Option<FList<ITEM>> =
    xs.foldRight(Some(Nil)) { o1: Option<ITEM>, o2: Option<FList<ITEM>> ->
        map2(o1, o2) { a1: ITEM, a2: FList<ITEM> ->
            Cons(a1, a2)
        }
    }

// 4.5
fun <A, B> traverse(xa: FList<A>, f: (A) -> Option<B>): Option<FList<B>> =
    when (xa) {
        is Nil -> Some(Nil)
        is Cons -> map2(f(xa.head), traverse(xa.tail, f)) { b, xb ->
            Cons(b, xb)
        }
    }

fun <A> sequence(xs: FList<Option<A>>): Option<FList<A>> = traverse(xs) {it}

// 4.6
fun <E, A, B> Either<E, A>.map(f: (A) -> B): Either<E, B> = when (this) {
    is Left-> this
    is Right-> Right(f(this.value))
}
fun <E, A> Either<E, A>.orElse(f: () -> Either<E, A>): Either<E, A> =
    when (this) {
        is Left -> f()
        is Right -> this
    }

fun <E, A, B> Either<E, A>.flatMap(f: (A) -> Either<E, B>): Either<E, B> =
    when (this) {
        is Left -> Left(value)
        is Right -> f(value)
    }

fun <E, A, B, C> map3( ae: Either<E, A>, be: Either<E, B>, f: (A, B) -> C): Either<E, C> =
    ae.flatMap { a-> be.map { b -> f(a, b) } }

// 4.7
fun <E, A, B> traverse2( xs : FList<A>, f: (A) -> Either<E, B> ): Either<E, FList<B>> =
    when (xs) {
        is Nil -> Right(Nil)
        is Cons ->
            map3(f(xs.head), traverse2(xs.tail, f)) { b, xb ->
                Cons(b, xb)
            }
    }

fun <E, A> sequence(es: FList<Either<E, A>>): Either<E, FList<A>> = traverse2(es) {it}

// 4.8
sealed class Partial<out A, out B>
data class Failures<out A>(val get: List<A>) : Partial<A, Nothing>()
data class Success<out B>(val get: B) : Partial<Nothing, B>()

sealed class Either<out E, out A>
data class Left<out E>(val value: E) : Either<E, Nothing>()
data class Right<out A>(val value: A) : Either<Nothing, A>()
sealed class Option<out ITEM>
data class Some<out ITEM>(val get: ITEM): Option<ITEM>()
object None: Option<Nothing>()
sealed class FList<out A> {
    companion object {
        fun <A> of(vararg aa: A): FList<A> {
            val tail: Array<out A> = aa.sliceArray(1 until aa.size)
            return if (aa.isEmpty()) Nil else Cons(aa[0], of(*tail))
        }

        fun sum(ints: FList<Int>): Int =
            when (ints) {
                is Nil -> 0
                is Cons -> ints.head + sum(ints.tail)
            }

        fun product(doubles: FList<Double>): Double =
            when (doubles) {
                is Nil -> 1.0
                is Cons ->
                    if (doubles.head == 0.0) 0.0
                    else doubles.head * product(doubles.tail)
            }

        // 3.1
        fun <A> tail(xs: FList<A>): FList<A> =
            when (xs) {
                is Nil -> Nil
                is Cons -> xs.tail
            }

        // 3.2
        fun <A> setHead(xs: FList<A>, x: A): FList<A> =
            when (xs) {
                is Nil -> Nil
                is Cons -> Cons(x, xs.tail)
            }

        // 3.3
        fun <A> drop(l: FList<A>, n: Int): FList<A> =
            if (n == 0) {
                l
            } else {
                when (l) {
                    is Nil -> Nil
                    is Cons -> drop(l.tail, n - 1)
                }
            }

        // 3.4
        fun <A> dropWhile(l: FList<A>, f: (A) -> Boolean): FList<A> =
            when (l) {
                is Nil -> Nil
                is Cons -> if (f(l.head)) dropWhile(l.tail, f) else l
            }

        fun <A> append(a1: FList<A>, a2: FList<A>): FList<A> =
            when (a1) {
                is Nil -> a2
                is Cons -> Cons(a1.head, append(a1.tail, a2))
            }

        // 3.5
        fun <A> init(l: FList<A>): FList<A> =
            when (l) {
                is Nil -> Nil
                is Cons -> {
                    if (l.tail == Nil) {
                        Nil
                    } else {
                        Cons(l.head, init(l.tail))
                    }
                }
            }

        fun <A, B> foldRight(xs: FList<A>, z: B, f: (A, B) -> B): B =
            when (xs) {
                is Nil -> z
                is Cons -> f(xs.head, foldRight(xs.tail, z, f))
            }

        // 3.6
        // X.

        // 3.7
        fun foldRightPassNilAndCons(): FList<Int> {
            return foldRight(Cons(1, Cons(2, Cons(3, Nil))), empty()) { x, y -> Cons(x, y) }
        }

        fun <A> empty(): FList<A> = Nil

        // 3.8
        fun <A> length(xs: FList<A>): Int =
            foldRight(xs, 0) { a, b -> 1 + b }

        // 3.9
        tailrec fun <A, B> foldLeft(xs: FList<A>, z: B, f: (B, A) -> B): B =
            when (xs) {
                is Nil -> z
                is Cons -> foldLeft(xs.tail, f(z, xs.head), f)
            }

        // 3.10
        fun sum3(ints: FList<Int>): Int = foldLeft(ints, 0) { b, a -> b + a }
        fun product3(dbs: FList<Double>): Double = foldLeft(dbs, 1.0) { b, a -> b * a }
        fun <A> length3(xs: FList<A>): Int = foldLeft(xs, 0) { b, a -> 1 + b }

        // 3.11
        fun <A> reverseList(xs: FList<A>): FList<A> =
            foldLeft(xs, empty()) { a: FList<A>, b: A -> Cons(b, a) }

        // 3.12 X
        fun <A, B> foldLeftR(xs: FList<A>, z: B, f: (B, A) -> B): B =
            foldRight(xs, { b: B -> b }, { a, g -> { b -> g(f(b, a)) } })(z)

        // 3.13
        fun <A> appendByFold(a1: FList<A>, a2: FList<A>): FList<A> =
            foldRight(a1, a2) { a, b -> Cons(a, b) }

        // 3.14 X
        fun <A> concat(xxs: FList<FList<A>>): FList<A> =
            foldRight(xxs, empty()) { xs1: FList<A>, xs2: FList<A> -> foldRight(xs1, xs2) { a, ls -> Cons(a, ls) } }

        fun <A> concat2(xxs: FList<FList<A>>): FList<A> =
            foldRight(xxs, empty()) { xs1: FList<A>, xs2: FList<A> -> append(xs1, xs2) }

        // 3.15
        fun increase(xs: FList<Int>): FList<Int> =
            foldRight(xs, empty()) { a: Int, b -> Cons(a + 1, b) }

        // 3.16
        fun doubleToString(xs: FList<Double>): FList<String> =
            foldRight(xs, empty()) { a, b -> Cons(a.toString(), b) }

        // 3.17
        fun <A, B> map(xs: FList<A>, f: (A) -> B): FList<B> =
            foldRight(xs, empty()) { a, b -> Cons(f(a), b) }

        // 3.18
        fun <A> filter(xs: FList<A>, f: (A) -> Boolean): FList<A> =
            foldRight(xs, empty()) { a, b ->
                if (f(a)) Cons(a, b)
                else b
            }

        // 3.19 X
        fun <A, B> flatMap(xa: FList<A>, f: (A) -> FList<B>): FList<B> =
            foldRight(xa, empty()) { a, lb -> append(f(a), lb) }

        // 3.20
        fun <A> filterByFlatMap(xs: FList<A>, f: (A) -> Boolean): FList<A> =
            flatMap(xs) { a -> if (f(a)) of(a) else empty() }

        // 3.21 X
        fun addSamePositionInList(xs: FList<Int>, xa: FList<Int>): FList<Int> =
            when (xs) {
                is Nil -> Nil
                is Cons -> {
                    when (xa) {
                        is Nil -> Nil
                        is Cons -> Cons(xs.head + xa.head, addSamePositionInList(xs.tail, xa.tail))
                    }
                }
            }

        // 3.22 X
        fun <A> zipWith(xa: FList<A>, xb: FList<A>, f: (A, A) -> A): FList<A> =
            when(xa) {
                is Nil -> Nil
                is Cons -> when(xb) {
                    is Nil -> Nil
                    is Cons -> Cons(f(xa.head, xb.head), zipWith(xa.tail, xb.tail, f))
                }
            }

        // 3.23 X
        tailrec fun <A> startWith(l1: FList<A>, l2: FList<A>): Boolean =
            when (l1) {
                is Nil -> l2 == Nil
                is Cons -> when (l2) {
                    is Nil -> true
                    is Cons -> {
                        if (l1.head == l2.head) startWith(l1.tail, l2.tail)
                        else false
                    }
                }
            }

        tailrec fun <A> hasSubsequence(xs: FList<A>, sub: FList<A>): Boolean =
            when(xs) {
                is Nil -> false
                is Cons -> if ( startWith(xs, sub)) true else hasSubsequence(xs.tail, sub)
            }
    }
}

object Nil: FList<Nothing>()
data class Cons<out A>(val head: A, val tail: FList<A>): FList<A>()
