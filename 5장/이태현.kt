sealed class FStream<out ITEM:Any> {
    data object Empty: FStream<Nothing>()
    data class Cons<out ITEM:Any>@PublishedApi internal constructor(@PublishedApi internal val head: () -> ITEM, @PublishedApi internal val tail: () -> FStream<ITEM>):
        FStream<ITEM>()

    companion object{
        //            inline operator fun <ITEM:Any> invoke(vararg items:ITEM): FStream<ITEM> = items.foldRight(invoke(), ::Cons)
        inline operator fun <ITEM:Any> invoke(): FStream<ITEM> = Empty
    }
}

fun <ITEM:Any> FStream<ITEM>.headOption(): Option<ITEM> =
    when (this) {
        is FStream.Empty -> Option.None
        is FStream.Cons -> Option.Some(head())
    }

fun <ITEM:Any> cons(hd: () -> ITEM, tl: () -> FStream<ITEM>): FStream<ITEM> {
    val head: ITEM by lazy(hd)
    val tail: FStream<ITEM> by lazy(tl)
    return FStream.Cons({ head }, { tail })
}

fun <ITEM:Any> empty(): FStream<ITEM> = FStream.Empty

fun <ITEM:Any> of(vararg xs: ITEM): FStream<ITEM> =
    if (xs.isEmpty()) empty()
    else cons({ xs[0] },
        { of(*xs.sliceArray(1 until xs.size))})

// 연습문제 5.1
fun <ITEM:Any> FStream<ITEM>.toList(): FList<ITEM> {
    tailrec fun go(xs: FStream<ITEM>, acc: FList<ITEM>): FList<ITEM> = when (xs) {
        is FStream.Empty -> acc
        is FStream.Cons -> go(xs.tail(), FList.Cons(xs.head(), acc))
    }
    return go(this, FList.Nil).reverse()
}

// 연습문제 5.2
fun <ITEM:Any> FStream<ITEM>.take(n: Int): FStream<ITEM> {
    fun go(xs: FStream<ITEM>, n: Int): FStream<ITEM> = when (xs) {
        is FStream.Empty -> empty()
        is FStream.Cons ->
            if (n == 0) empty()
            else cons(xs.head, { go(xs.tail(), n - 1) })
    }
    return go(this, n)
}

fun <ITEM:Any> FStream<ITEM>.drop(n: Int): FStream<ITEM> {
    tailrec fun go(xs: FStream<ITEM>, n: Int): FStream<ITEM> = when (xs) {
        is FStream.Empty -> empty()
        is FStream.Cons ->
            if (n == 0) xs
            else go(xs.tail(), n - 1)
    }
    return go(this, n)
}

// 연습문제 5.3
fun <ITEM:Any> FStream<ITEM>.takeWhile(p: (ITEM) -> Boolean): FStream<ITEM> =
    when (this) {
        is FStream.Empty -> empty()
        is FStream.Cons ->
            if (p(this.head()))
                cons(this.head, { this.tail().takeWhile(p) })
            else empty()
    }

fun <ITEM:Any> FStream<ITEM>.exists(p: (ITEM) -> Boolean): Boolean =
    when (this) {
        is FStream.Cons -> p(this.head()) || this.tail().exists(p)
        else -> false
    }

fun <ITEM:Any, OTHER:Any> FStream<ITEM>.foldRight(
    z: () -> OTHER,
    f: (ITEM, () -> OTHER) -> OTHER
): OTHER =
    when (this) {
        is FStream.Cons -> f(this.head()) {
            tail().foldRight(z, f)
        }
        is FStream.Empty -> z()
    }

// 연습문제 5.4
fun <ITEM:Any> FStream<ITEM>.forAll(p: (ITEM) -> Boolean): Boolean =
    foldRight({ true }, { a, b -> p(a) && b() })

// 연습문제 5.5
fun <ITEM:Any> FStream<ITEM>.takeWhile2(p: (ITEM) -> Boolean): FStream<ITEM> =
    foldRight({ empty() },
        { h, t -> if (p(h)) cons({ h }, t) else t() })

// 연습문제 5.6
//    fun <ITEM:Any> FStream<ITEM>.headOption: Option<ITEM> =

// 연습문제 5.7
fun <ITEM:Any, OTHER:Any> FStream<ITEM>.map(f: (ITEM) -> OTHER): FStream<OTHER> =
    foldRight(
        { empty<OTHER>()},
        { h, t -> cons({ f(h) }, t)}
    )

fun <ITEM: Any> FStream<ITEM>.filter(f: (ITEM) -> Boolean): FStream<ITEM> =
    foldRight(
        { empty<ITEM>() },
        { h, t -> if (f(h)) cons({ h }, t) else t() }
    )

fun <ITEM:Any> FStream<ITEM>.append(sa: () -> FStream<ITEM>): FStream<ITEM> =
    foldRight(sa) { h, t -> cons({ h }, t) }

fun <ITEM:Any, OTHER:Any> FStream<ITEM>.flatMap(f: (ITEM) -> FStream<OTHER>): FStream<OTHER> =
    foldRight(
        { empty<OTHER>() },
        { h, t -> f(h).append(t) }
    )

// 연습문제 5.8
fun <ITEM:Any> FStream<ITEM>.constant(a: ITEM): FStream<ITEM> =
    cons({ a }, { constant(a) })

// 연습문제 5.9
fun <ITEM:Any> FStream<ITEM>.from(n: Int): FStream<Int> =
    cons({ n }, { from(n + 1) })

// 연습문제 5.10
fun <ITEM:Any> FStream<ITEM>.fibs(): FStream<Int> {
    fun go(curr: Int, nxt: Int): FStream<Int> =
        cons({ curr }, { go(nxt, curr + nxt) })
    return go(0, 1)
}

// 연습문제 5.11
fun <ITEM:Any, OTHER:Any> unfold(z: OTHER, f: (OTHER) -> Option<Pair<ITEM, OTHER>>): FStream<ITEM> =
    f(z).map { pair ->
        cons({ pair.first },
            { unfold(pair.second, f) })
    }.getOrElse { empty() }

// 연습문제 5.12

// 연습문제 5.13
fun <ITEM:Any, OTHER:Any> FStream<ITEM>.zipAll(
    that: FStream<OTHER>
): FStream<Pair<Option<ITEM>, Option<OTHER>>> =
    unfold(this to that) { (ths, tht) ->
        when (ths) {
            is FStream.Cons -> when (tht) {
                is FStream.Cons ->
                    Option.Some(
                        Pair(
                            Option.Some(ths.head()) to Option.Some(tht.head()),
                            ths.tail() to tht.tail()
                        )
                    )
                else ->
                    Option.Some(
                        Pair(
                            Option.Some(ths.head()) to Option.None,
                            ths.tail() to empty<OTHER>()
                        )
                    )
            }
            else -> when (tht) {
                is FStream.Cons ->
                    Option.Some(
                        Pair(
                            Option.None to Option.Some(tht.head()),
                            empty<ITEM>() to tht.tail()
                        )
                    )
                else -> Option.None
            }
        }
    }

// 연습문제 5.14

// 연습문제 5.15
fun <ITEM:Any> FStream<ITEM>.tails(): FStream<FStream<ITEM>> =
    unfold(this) { s: FStream<ITEM> ->
        when (s) {
            is FStream.Cons ->
                Option.Some(s to s.tail())
            else -> Option.None
        }
    }

// 연습문제 5.16
fun <ITEM:Any, OTHER:Any> FStream<ITEM>.scanRight(z: OTHER, f: (ITEM, () -> OTHER) -> OTHER): FStream<OTHER> =
    foldRight({ z to of(z)},
        { a: ITEM, p0: () -> Pair<OTHER, FStream<OTHER>> ->
            val p1: Pair<OTHER, FStream<OTHER>> by lazy { p0() }
            val b2: OTHER = f(a) { p1.first }
            Pair<OTHER, FStream<OTHER>>(b2, cons({ b2 }, { p1.second }))
        }).second