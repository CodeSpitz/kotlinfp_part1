// 5.1
fun <A> Stream<A>.toList(): List<A> {
    tailrec fun <A> makeList(target: Stream<A>, tail: List<A>): List<A> =
        when (target) {
            is Empty -> tail
            is Cons -> makeList(target.tail(), chapter3.Cons(target.head(), tail))
        }
    return reverse(makeList(this, List.empty()))
}

// 5.2
fun <A> Stream<A>.take(n: Int): Stream<A> =
    when (this) {
        is Empty -> this
        is Cons ->
            if (n == 0) Empty
            else Stream.cons(this.head) { this.tail().take(n - 1) }
    }

fun <A> Stream<A>.drop(n: Int): Stream<A> {
    tailrec fun <A> dropFn(target: Stream<A>, n: Int): Stream<A> = when (target) {
        is Empty -> Empty
        is Cons -> if (n > 0) dropFn(target.tail(), n - 1) else target
    }

    return dropFn(this, n)
}

// 5.3
fun <A> Stream<A>.takeWhile(p: (A) -> Boolean): Stream<A> {
    fun <A> takeWhileFn(target: Stream<A>, p: (A) -> Boolean): Stream<A> = when (target) {
        is Empty -> Empty
        is Cons ->
            if (p(target.head())) Stream.cons(target.head) { takeWhileFn(target.tail(), p) }
            else Empty
    }

    return takeWhileFn(this, p)
}

// 5.4
fun <A> Stream<A>.forAll(p: (A) -> Boolean): Boolean =
    when(this) {
        is Cons -> p(this.head()) && this.tail().forAll(p)
        is Empty -> true
    }

// 5.5
fun <A> Stream<A>.takeWhile(p: (A) -> Boolean): Stream<A> =
    foldRight({ empty() }, { a, b ->
        if (p(a)) Stream.cons({ a }) { b().takeWhile(p) } else empty()
    })

// 5.6
fun <A> Stream<A>.headOption(): Option<A> =
    foldRight({ Option.empty() }, { a, _ -> Some(a) })

// 5.7
fun <A, B> Stream<A>.map(f: (A) -> B): Stream<B> =
    foldRight({ empty() }, { a, b -> Stream.cons({ f(a) }, b) })

fun <A> Stream<A>.filter(f: (A) -> Boolean): Stream<A> =
    foldRight({ empty() }, { a, b -> if (f(a)) Stream.cons({ a }, b) else b().filter(f) })

fun <A> Stream<A>.append(sa: () -> Stream<A>): Stream<A> =
    foldRight(sa) { a, b -> Stream.cons({ a }, b) }

fun <A, B> Stream<A>.flatMap(f: (A) -> Stream<B>): Stream<B> =
    foldRight({ empty() }, { a, b -> f(a).append(b) })

// 5.8
fun <A> constant(a: A): Stream<A> =
    Stream.cons({ a }, { constant(a) })

// 5.9
fun from(n: Int): Stream<Int> =
    Stream.cons({ n }, { from(n + 1) })

// 5.10
fun fibs(): Stream<Int> {
    fun fibsFunc(now: Int, next: Int): Stream<Int> {
        return Stream.cons({ now }, { fibsFunc(next, now + next) })
    }
    return fibsFunc(0, 1)
}
// 5.11
fun <A, S> unfold(z: S, f: (S) -> Option<Pair<A, S>>): Stream<A> =
    f(z).map { pair ->
        Stream.cons({ pair.first }, { unfold(pair.second, f) })
    }.getOrElse { Empty }

// 5.12
fun fibs(): Stream<Int> =
    Stream.unfold(0 to 1) { (now, next) -> Some(now to (next to now + next)) }

fun from(n: Int): Stream<Int> =
    Stream.unfold(n) { z -> Some(z to z + 1) }

fun <A> constant(n: A): Stream<A> =
    Stream.unfold(n) { z -> Some(z to z) }

fun ones(): Stream<Int> =
    Stream.unfold(1) { z -> Some(z to z) }

// 5.13
fun <A, B> Stream<A>.map(f: (A) -> B): Stream<B> =
    Stream.unfold(this) { s: Stream<A> ->
        when (s) {
            is Empty -> None
            is Cons -> Some(f(s.head()) to s.tail())
        }
    }

fun <A> Stream<A>.take(n: Int): Stream<A> =
    Stream.unfold(this to n) { (s: Stream<A>, index: Int) ->
        when (s) {
            is Empty -> None
            is Cons -> {
                if (index == 0) None
                else Some(s.head() to (s.tail() to index - 1))
            }
        }
    }

fun <A> Stream<A>.takeWhile(p: (A) -> Boolean): Stream<A> =
    Stream.unfold(this) { s: Stream<A> ->
        when (s) {
            is Empty -> None
            is Cons -> if (p(s.head())) Some(s.head() to s.tail()) else None
        }
    }

fun <A, B, C> Stream<A>.zipWith(
    that: Stream<B>,
    f: (A, B) -> C
): Stream<C> =
    Stream.unfold(this to that) { (a: Stream<A>, b: Stream<B>) ->
        when (a) {
            is Empty -> None
            is Cons -> when (b) {
                is Empty -> None
                is Cons -> Some(f(a.head(), b.head()) to (a.tail() to b.tail()))
            }
        }
    }

fun <A, B> Stream<A>.zipAll(
    that: Stream<B>
): Stream<Pair<Option<A>, Option<B>>> =
    Stream.unfold(this to that) { (a: Stream<A>, b: Stream<B>) ->
        when (a) {
            is Empty -> when (b) {
                is Empty -> None
                is Cons -> Some(None to Some(b.head()) to (Empty to b.tail()))
            }

            is Cons -> when (b) {
                is Empty -> Some(Some(a.head()) to None to (a.tail() to Empty))
                is Cons -> Some(Some(a.head()) to Some(b.head()) to (a.tail() to b.tail()))
            }
        }
    }

// 5.14
fun <A> Stream<A>.startsWith(that: Stream<A>): Boolean =
    this.zipAll(that)
        .takeWhile { it.second != None }
        .forAll { it.first == it.second }

// 5.15
fun <A> Stream<A>.tails(): Stream<Stream<A>> =
    Stream.unfold(this) { s: Stream<A> ->
        when (s) {
            is Empty -> None
            is Cons -> Some(s to s.tail())
        }
    }

// 5.16
fun <A, B> Stream<A>.scanRight(z: B, f: (A, () -> B) -> B): Stream<B> =
    foldRight({ z to Stream.of(z) },
        { a: A, b: () -> Pair<B, Stream<B>> ->
            val head: Pair<B, Stream<B>> by lazy { b() }
            val tail: B = f(a) { head.first }
            Pair(tail, Stream.cons({ tail }, { head.second }))
        }).second
