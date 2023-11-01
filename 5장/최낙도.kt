package study.fp

import study.fp.FStream.*
import study.fp.FOption.*

sealed class FStream<out A> {
    data class Cons<out A>(val head: () -> A, val tail: () -> FStream<A>) : FStream<A>()
    object Empty : FStream<Nothing>()
}

fun <A> FStream<A>.headOption(): FOption<A> =
    when (this) {
        is Empty -> None
        is Cons -> Some(head())
    }
fun <A> cons(hd: () -> A, tl: () -> FStream<A>): FStream<A> {
    val head: A by lazy(hd)
    val tail: FStream<A> by lazy(tl)
    return Cons({ head }, { tail })
}
fun <A> empty(): FStream<A> = Empty
fun <A> FStream<A>.of(vararg xs: A): FStream<A> =
    if (xs.isEmpty()) empty()
    else cons({ xs[0] }, { of(*xs.sliceArray(1 until  xs.size)) })
fun <A> FStream<A>.exists(p: (A) -> Boolean): Boolean =
    when (this) {
        is Cons -> p(this.head()) || this.tail().exists(p)
        else -> false
    }
fun <A, B> FStream<A>.foldRight(z: () -> B, f: (A, () -> B) -> B): B =
    when (this) {
        is Cons -> f(this.head()) {
            tail().foldRight(z, f)
        }
        is Empty -> z()
    }

/* 연습문제 5.1 */
fun <A> FStream<A>.toList(): FList<A> {
    tailrec fun loop(stream: FStream<A>, list: FList<A>): FList<A> =
        when (stream) {
            is Cons -> loop(stream.tail(), FList.Cons(stream.head(), list))
            is Empty -> list
        }
    return loop(this, FList.Nil)
}

/* 연습문제 5.2 */
fun <A> FStream<A>.take(n: Int): FStream<A> {
    tailrec fun loop(i: Int, stream: FStream<A>): FStream<A> =
        when (stream) {
            is Cons -> if (i == 0) empty() else Cons(stream.head) { loop(i - 1, stream.tail()) }
            is Empty -> empty()
        }
    return loop(n, this)
}
fun <A> FStream<A>.drop(n: Int): FStream<A> {
    tailrec fun loop(i: Int, stream: FStream<A>): FStream<A> =
        when (stream) {
            is Cons -> if (i == 0) stream else loop(i - 1, stream.tail())
            is Empty -> empty()
        }
    return loop(n, this)
}

/* 연습문제 5.3 */
fun <A> FStream<A>.takeWhile(check: (A) -> Boolean): FStream<A> {
    tailrec fun loop(check: (A) -> Boolean) =
        when (this) {
            is Cons -> if (check(head())) Cons(head) { this.tail().takeWhile(check) } else empty()
            is Empty -> empty()
        }
    return loop(check)
}

/* 연습문제 5.4 */
fun <A> FStream<A>.forAll(check: (A) -> Boolean): Boolean =
    foldRight({ true }, { a, b -> check(a) && b() })

/* 연습문제 5.5 */
fun <A> FStream<A>.takeWhile2(check: (A) -> Boolean): FStream<A> =
    foldRight({ empty() }, { a, b -> if (check(a)) cons({ a }, b) else b() })


/* 연습문제 5.6 */
fun <A> FStream<A>.headOption2(): FOption<A> =
    this.foldRight( { FOption.None }, { a, _ -> Some(a) } )

/* 연습문제 5.7 */
fun <A, B> FStream<A>.map2(f: (A) -> B): FStream<B> =
    this.foldRight( { empty<B>() }, { h, t -> if (f(h)) cons({ h }, t) else t() })
fun <A> FStream<A>.filter(f: (A) -> Boolean): FStream<A> =
    this.foldRight({ empty<A>() }, { h, t -> if (f(h)) cons({ h }, t) else t() })
fun <A> FStream<A>.append(sa: () -> FStream<A>): FStream<A> =
    this.foldRight(sa) { h, t -> cons({ h }, t) }
fun <A, B> FStream<A>.flatMap(f: (A) -> FStream<B>): FStream<B> =
    this.foldRight({ empty<B>() }, { h, t -> f(h).append { t() } })

/* 연습문제 5.8 */
fun <A> FStream<A>.constant(a: A): FStream<A> =
    cons({ a }, { constant(a) })

/* 연습문제 5.9 */
fun FStream<Int>.from(n: Int): FStream<Int> =
    cons({ n }, { from(n + 1) })

/* 연습문제 5.10 */
fun FStream<Int>.fibs(): FStream<Int> {
    tailrec fun loop(curr: Int, nxt: Int): FStream<Int> =
        cons({ curr }, { loop(nxt, curr + nxt) })
    return loop(0, 1)
}

/* 연습문제 5.11 */
fun <A, S> unfold(z: S, f: (S) -> FOption<Pair<A, S>>): FStream<A> =
    f(z).map { pair -> cons({ pair.first }, { unfold(pair.second, f) })}
        .getOrElse { empty() }

/* 연습문제 5.12 */
fun FStream<Int>.fibs2(): FStream<Int> =
    unfold(0 to 1) { (curr, next) -> Some(curr to (next to (curr + next))) }
fun FStream<Int>.from2(n: Int): FStream<Int> =
    unfold(n) { a -> Some(a to (a + 1)) }
fun <A> FStream<A>.constant2(n: A): FStream<A> =
    unfold(n) { a -> Some(a to a) }
fun FStream<Int>.ones(n: Int): FStream<Int> =
    unfold(1) { a -> Some(1 to 1) }

/* 연습문제 5.13 */
fun <A, B> FStream<A>.map(f: (A) -> B): FStream<B> =
    unfold(this) { s: FStream<A> ->
        when (s) {
            is Cons -> Some(f(s.head()) to s.tail())
            else -> None
        }
    }
fun <A> FStream<A>.take2(n: Int): FStream<A> =
    unfold(this) { s: FStream<A> ->
        when (s) {
            is Cons -> if (n > 0) Some(s.head() to s.tail().take(n - 1)) else None
            else -> None
        }
    }
fun<A> FStream<A>.takeWhile3(p: (A) -> Boolean): FStream<A> =
    unfold(this
    ) { s: FStream<A> ->
        when (s) {
            is Cons -> if (p(s.head())) Some(s.head() to s.tail()) else None
            else -> None
        }
    }
fun <A, B, C> FStream<A>.zipWith(that: FStream<B>, f: (A, B) -> C): FStream<C> =
    unfold(this to that) { (ths: FStream<A>, tht: FStream<B>) ->
        when (ths) {
            is Cons -> when (tht) {
                is Cons -> Some(Pair(f(ths.head(), tht.head()), ths.tail() to tht.tail()))
                else -> None
            }
            else -> None
        }
    }
fun <A, B> FStream<A>.zipAll(that: FStream<B>): FStream<Pair<FOption<A>, FOption<B>>> =
    unfold(this to that) { (ths, tht) ->
        when (ths) {
            is Cons -> when (tht) {
                is Cons -> Some(Pair(Some(ths.head()) to Some(tht.head()), ths.tail() to tht.tail()))
                else -> Some(Pair(Some(ths.head()) to None, ths.tail() to empty<B>()))
            }
            else -> when (tht) {
                is Cons -> Some(Pair(None to Some(tht.head()), empty<A>() to tht.tail()))
                else -> None
            }
        }
    }


/* 연습문제 5.14 */
fun <A> FStream<A>.startsWith(that: FStream<A>): Boolean =
    this.zipAll(that)
        .takeWhile { !it.second.isEmpty() }
        .forAll { it.first == it.second }

/* 연습문제 5.15 */
fun <A> FStream<A>.tails(): FStream<FStream<A>> =
    unfold(this) { s: FStream<A> ->
        when (s) {
            is Cons -> Some(s to s.tail())
            else -> None
        }
    }

/* 연습문제 5.16 */
fun <A, B> FStream<A>.scanRight(z: B, f: (A, () -> B) -> B): FStream<B> =
    foldRight({ (z to of(z)) as Pair<B, FStream<B>> },
        { a: A, p0: () -> Pair<B, FStream<B>> ->
            val p1: Pair<B, FStream<B>> by lazy { p0() }
            val b2: B = f(a) { p1.first }
            Pair<B, FStream<B>>(b2, cons({ b2 }, { p1.second }) as FStream<B>)
        }).second





