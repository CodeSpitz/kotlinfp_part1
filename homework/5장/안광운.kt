sealed class Stream<out A>

data class Cons<out A>(
    val head:() -> A,
    val tail:() -> Stream<A>
): Stream<A>()

object Empty: Stream<Nothing>()

fun <A> Stream<A>.tolist(): List<A> {
    tailrec fun go(xs: Stream<A>, ace: List<A>): List<A> = when(xs) {
        is Empty-> ace
        is Cons-> go(xs.tail(), ConsL(xs.head(), ace))
    }
    return reverse(go(this, Nill))
}

fun <A> Stream<A>.take(n: Int): Stream<A> {
    fun go(xs: Stream<A>, n: Int ) : Stream<A> = when (xs)
    {
        is Empty-> empty() is Cons->
            if (n == 0) empty()
            else cons(xs.head, { go(xs.tail(), n - 1) })
    }
    return go(this, n)
}

fun <A> Stream<A>.drop(n: Int ) : Stream<A> {
    tailrec fun go(xs: Stream<A>, n: Int): Stream<A> = when (xs) {
        is Empty-> empty()
        is Cons-> if (n == 0) XS else go(xs.tail(), n - 1)
    }
    return go(this, n)
}

fun <A> Stream<A>.takeWhile(p: (A) -> Boolean): Stream<A> = when (this) {
    is Empty -> empty()
    is Cons ->
        if (p(this.head())) cons(this.head, { this.tail().takeWhile(p) })
        else empty()
}

fun <A> Stream<A>.forAll(p: (A) -> Boolean): Boolean= foldRight({ true}, { a, b -> p(a) && b() })

fun <A> Stream<A>.takeWhile(p: (A) -> Boolean): Stream<A> = foldRight({ empty() },
    { h, t -> if (p(h)) cons({ h }, t) else t() })

fun <A> Stream<A>.headOption(): Option<A> = this.foldRight(
    { Option.empty() },
    {a,_-> Some(a) }
)

fun <A, B> Stream<A>.map(f: (A) -> B): Stream<B> = this.foldRight(
    { empty<B>() },
    { h, t -> cons({ f(h) }, t) }
)

fun <A> Stream<A>.filter(f: (A) -> Boolean): Stream<A> = this.foldRight(
    { empty<A>() },
    { h, t -> if (f(h)) cons({ h }, t) else t() }
)

fun <A> Stream<A>.append(sa: () -> Stream<A>): Stream<A> = foldRight(sa) { h, t ->
    cons({ h }, t)
}

fun <A, B> Stream<A>.flatMap(f: (A) -> Stream<B>): Stream<B> = foldRight(
    { empty<B>() },
    { h, t -> f(h).append(t) }
)

fun <A> constant(a: A): Stream<A> = Stream.cons({ a }, { constant(a) })

fun from(n: Int ) : Stream<Int> = cons({ n }, { from(n + 1) })

fun fibs(): Stream<Int> {
    fun go(curr: Int, nxt: Int): Stream<Int> =
        cons({ curr }, { go(nxt, curr + nxt) })
    return go(0, 1)
}

fun <A, S> unfold(z: S, f: (S) -> Option<Pair<A, 5>>): Stream<A> = f(z).map { pair ->
    cons({ pair.first },
        { unfold(pair.second, f) })
}.getOrElse {
    empty()
}

fun fibs(): Stream<Int> = Stream.unfold(0 to 1, {
        (curr, next) -> Some(curr to (next to (curr + next)))
}
)

fun from(n: Int): Stream<Int> = Stream.unfold(n, { a -> Some(a to (a + 1)) })

fun <A> constant(n: A): Stream<A> = Stream.unfold(n, { a -> Some(a to a) })

fun ones(): Stream<Int> = Stream.unfold(l, { Some(l to 1) })

fun <A, B> Stream<A>.map(f: (A) -> B): Stream<B> = Stream.unfold(this) { s: Stream<A> ->
    when (s) {
        is Cons -> Some(f(s.head()) to s.tail())
        else -> None
    }
}

fun <A> Stream<A>.take(n: Int ) : Stream<A> = Stream.unfold(this) { s: Stream<A> ->
    when (s) {
        is Cons ->
            if (n > 0)
                Some(s.head() to s.tail().take(n - 1))
            else None

        else -> None
    }
}

fun <A> Stream<A>.takeWhile(p: (A) -> Boolean): Stream<A> = Stream.unfold(this,
    { s: Stream<A> ->
        when (s) {
            is Cons ->
                if (p(s.head()))
                    Some(s.head() to s.tail()) else None
            else -> None
        }
    }
)

fun <A, B, C> Stream<A>.zipWith(
    that: Stream<B>,
    f: (A, B) -> C
): Stream<C> =
    Stream.unfold(this to that) { (ths: Stream<A>, tht: Stream<B>) ->
        when (ths) {
            is Cons ->
                when (tht) {
                    is Cons -> Some(
                        Pair(
                            f(ths.head(), tht.head()), ths.tail() to tht.tail()
                        )
                    )
                    else -> None
                }
            else -> None
        }
    }

fun <A, B> Stream<A>.zipAll( that: Stream<B>): Stream<Pair<Option<A>, Option<B>>> = Stream.unfold(this to that) { (ths, tht) ->
    when (ths) {
        is Cons -> when (tht) {
            is Cons -> Some(
                Pair(
                    Some(ths.head()) to Some(tht.head()), ths.tail() to tht.tail()
                )
            )

            else -> Some(
                Pair(
                    Some(ths.head()) to None, ths.tail() to Stream.empty<B>()
                )
            )
        }

        else -> when (tht) {
            is Cons -> Some(
                Pair(
                    None to Some(tht.head()), Stream.empty<A>() to tht.tail()
                )
            )
            else -> None
        }
    }
}

fun <A> Stream<A>.startsWith(that: Stream<A>): Boolean = this.zipAll(that)
    .takeWhile { !it.second.isEmpty() }
    .forAll { it.first == it.second }

fun <A> Stream<A>.tails(): Stream<Stream<A>> = Stream.unfold(this) { s: Stream<A> ->
    when (s) {
        is Cons -> Some(s to s.tail())
        else -> None
    }
}

fun <A, B> Stream<A>.scanRight(z: B, f: (A, () -> B) -> B): Stream<B> = foldRight({ z to Stream.of(z) },
    { a: A, p0: () -> Pair<B, Stream<B>> ->
        val pl: Pair<B, Stream<B>> by lazy { p0() }
        val b2: B = f(a) { pl.first }
        Pair<B, Stream<B>>(b2, cons({ b2 }, { pl.second }))
    }).second



