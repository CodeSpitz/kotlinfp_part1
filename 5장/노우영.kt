package chapter5

import chpater4.*


sealed class FStream<out A> {
    companion object {
        fun <A> of(vararg aa: A): FStream<A> {
            val tail = aa.sliceArray(1 until aa.size)
            return if (aa.isEmpty()) empty() else cons({ aa[0] }, { of(*tail) })
        }
    }
}

data class Cons<out A>(
    val head: () -> A,
    val tail: () -> FStream<A>
) : FStream<A>()

object Empty : FStream<Nothing>()


fun <A> cons(hd: () -> A, tl: () -> FStream<A>): FStream<A> {
    val head: A by lazy { hd() }
    val tail: FStream<A> by lazy { tl() }
    return Cons({ head }, { tail })
}

fun <A> empty(): FStream<A> = Empty

//5.1
fun <A> FStream<A>.toListUnsafe(): List<A> {
    tailrec fun go(s: FStream<A>, acc: List<A>): List<A> = when (s) {
        is Empty -> acc
        is Cons -> go(s.tail(), acc + s.head())
    }
    return go(this, listOf())
}

//5.2
fun <A> FStream<A>.take(n: Int): FStream<A> = when (this) {
    is Empty -> empty()
    is Cons -> if (n <= 0) empty() else cons(head) { tail().take(n - 1) }
}

fun <A> FStream<A>.drop(n: Int): FStream<A> = when (this) {
    is Empty -> empty()
    is Cons -> if (n <= 0) this else tail().drop(n - 1)
}

//5.3
fun <A> FStream<A>.takeWhile(p: (A) -> Boolean): FStream<A> = when (this) {
    is Empty -> empty()
    is Cons -> if (p(head())) cons(head) { tail().takeWhile(p) } else empty()
}

fun <A, B> FStream<A>.foldRightC5(
    z: () -> B,
    f: (A, () -> B) -> B
): B = when (this) {
    is Cons -> f(head()) {
        tail().foldRightC5(z, f)
    }

    is Empty -> z()
}

fun <A> FStream<A>.exists2(p: (A) -> Boolean): Boolean =
    foldRightC5({ false }) { a, b ->
        p(a) || b()
    }


fun <B> FStream<B>.exists(p: (B) -> Boolean): Boolean = when (this) {
    is Empty -> false
    is Cons -> p(head()) || tail().exists(p)
}

//5.4
fun <A> FStream<A>.forAll(p: (A) -> Boolean): Boolean = when (this) {
    is Empty -> true
    is Cons -> p(head()) && tail().forAll(p)
}

//5.5
fun <A> FStream<A>.takeWhile2(p: (A) -> Boolean): FStream<A> =
    foldRightC5({ empty() }) { a, b ->
        if (p(a)) cons({ a }, b) else empty()
    }

//5.6
fun <A> FStream<A>.headOption(): Option<A> = when (this) {
    is Empty -> None
    is Cons -> Some(head())
}

fun <A> FStream<A>.headOption2(): Option<A> =
    foldRightC5({ Option.empty() }) { a, b ->
        Some(a)
    }

//5.7
fun <A> FStream<A>.map(p: (A) -> A): FStream<A> =
    foldRightC5({ empty() }) { a, b ->
        cons({ p(a) }, b)
    }

fun <A> FStream<A>.filter(p: (A) -> Boolean): FStream<A> =
    foldRightC5({ empty() }) { a, b ->
        if (p(a)) cons({ a }, b) else b()
    }

fun <A> FStream<A>.append(a: () -> A): FStream<A> =
    foldRightC5({ cons(a) { empty() } }) { a, b ->
        cons({ a }, b)
    }

fun ones(): FStream<Int> = cons({ 1 }) { ones() }

//5.8
fun <A> constant(a: A): FStream<A> = cons({ a }) { constant(a) }

//5.9
fun from(n: Int): FStream<Int> = cons({ n }) { from(n + 1) }

//5.10
fun fibs(): FStream<Int> {
    fun go(n: Int, m: Int): FStream<Int> = cons({ n }) { go(m, n + m) }
    return go(0, 1)
}

//5.11
fun <A, B> unfold(z: B, f: (B) -> Option<Pair<A, B>>): FStream<A> {
    val p = f(z)
    return when (p) {
        is None -> empty()
        is Some -> cons({ p.get.first }) { unfold(p.get.second, f) }
    }
}

//5.12
fun fibs2(): FStream<Int> = unfold(Pair(0, 1)) { p ->
    Some(Pair(p.first, Pair(p.second, p.first + p.second)))
}

fun from2(n: Int): FStream<Int> = unfold(n) { p ->
    Some(Pair(p, p + 1))
}

fun <A> constant2(a: A): FStream<A> = unfold(a) { p ->
    Some(Pair(p, p))
}

//5.13
fun <A, B> FStream<A>.map2(p: (A) -> B): FStream<B> = unfold(this) { s ->
    when (s) {
        is Empty -> None
        is Cons -> Some(Pair(p(s.head()), s.tail()))
    }
}

fun <A> FStream<A>.take2(n: Int): FStream<A> = unfold(this) { s ->
    when (s) {
        is Empty -> None
        is Cons -> if (n <= 0) None else Some(Pair(s.head(), s.tail().take2(n - 1)))
    }
}

fun <A> FStream<A>.takeWhile3(p: (A) -> Boolean): FStream<A> = unfold(this) { s ->
    when (s) {
        is Empty -> None
        is Cons -> if (p(s.head())) Some(Pair(s.head(), s.tail())) else None
    }
}

fun <A, B, C> FStream<A>.zipWith(
    that: FStream<B>,
    f: (A, B) -> C
): FStream<C> = unfold(this to that) { (ths, tht) ->
    when (ths) {
        is Empty -> None
        is Cons -> when (tht) {
            is Empty -> None
            is Cons -> Some(Pair(f(ths.head(), tht.head()), ths.tail() to tht.tail()))
        }
    }
}

fun <A, B> FStream<A>.zipAll(
    that: FStream<B>
): FStream<Pair<Option<A>, Option<B>>> = unfold(this to that) { (ths, tht) ->
    when (ths) {
        is Empty -> when (tht) {
            is Empty -> None
            is Cons -> Some(Pair(Pair(None, Some(tht.head())), Empty to tht.tail()))
        }

        is Cons -> when (tht) {
            is Empty -> Some(Pair(Pair(Some(ths.head()), None), ths.tail() to Empty))
            is Cons -> Some(Pair(Pair(Some(ths.head()), Some(tht.head())), ths.tail() to tht.tail()))
        }
    }
}

//5.14
fun <A> FStream<A>.startsWith(that: FStream<A>): Boolean =
    this.zipAll(that).takeWhile3 { !it.second.isEmpty() }.forAll { it.first == it.second }

//5.15
fun <A> FStream<A>.tails(): FStream<FStream<A>> = unfold(this) { s ->
    when (s) {
        is Empty -> None
        is Cons -> Some(Pair(s, s.tail()))
    }
}

//5.16
fun <A, B> FStream<A>.scanRight(z: B, f: (A, () -> B) -> B): FStream<B> =
    foldRightC5({ z to FStream.of(z) }) { a, p0 ->
        val p1 by lazy { p0() }
        val b2 = f(a) { p1.first }
        b2 to cons({ b2 }) { p1.second }
    }.second
