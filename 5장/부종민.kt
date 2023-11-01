    fun <A> Stream<A>.toList(): List<A> = when(this) {
        is Cons -> {
            ConsL(this.head(), this.tail().toList())
        }
        is Empty -> {
            Nil
        }
    }

    fun <A> Stream<A>.take(n: Int): Stream<A> = when(this) {
        is Cons -> {
            if(n == 0) {
                Empty
            } else {
                cons(this.head, {this.tail().take(n-1)})
            }
        }
        is Empty -> Empty
    }

    fun <A> Stream<A>.drop(n: Int): Stream<A> = when(this) {
        is Cons -> {
            if(n == 0)  this
            else this.tail().drop(n - 1)
        }
        is Empty -> empty()
    }

    fun <A> Stream<A>.takeWhile(p: (A) -> Boolean): Stream<A> = when(this) {
        is Cons -> {
            if(p(this.head()))  cons(this.head) { this.tail().takeWhile(p) }
            else empty()
        }
        is Empty -> empty()
    }

fun <A> Stream<A>.forAll(p: (A) -> Boolean): Boolean = when(this) {
    is Cons -> {
        if(p(this.head())) this.tail().forAll(p)
        else false
    }
    is Empty -> true
}


    fun <A> Stream<A>.takeWhile(p: (A) -> Boolean): Stream<A>
        = foldRight({empty()}, {h, t -> if(p(h)) {cons({h}, t)} else empty() } )


    fun <A> Stream<A>.headOption(): Option<A> = when(this) {
        is Cons -> Some(this.head())
        is Empty -> None
    }


    fun <A, B> Stream<A>.map(f: (A) -> B): Stream<B> = foldRight({empty()}, {h, t -> cons({f(h)}, t)})

    fun <A> Stream<A>.filter(f: (A) -> Boolean): Stream<A> = foldRight({empty()}, {h, t -> if(f(h)) { cons({h}, t) } else t() })

    fun <A> Stream<A>.append(sa: () -> Stream<A>): Stream<A> = foldRight(sa){ h, t -> cons({h}, t) }

    fun <A, B> Stream<A>.flatMap(f: (A) -> Stream<B>): Stream<B> = foldRight({empty()}, {h, t -> f(h).append(t)})

    fun <A> constant(a: A): Stream<A> = Stream.cons({a}, {constant(a)})

    fun from(n: Int): Stream<Int> = Stream.cons({n}, {from(n+1)})

        fun fibs(): Stream<Int> {
        fun go(curr: Int, next: Int): Stream<Int> = cons({curr}, {go(next, curr + next )})

        return go(0, 1)
    }

fun <A, S> unfold(z: S, f: (S) -> Option<Pair<A, S>>): Stream<A> = f(z).map { pair ->
    Stream.cons({ pair.first },
        { chapter5.solutions.ex11.unfold(pair.second, f) })
    }.getOrElse {
        empty()
    }


    fun fibs(): Stream<Int> = Stream.unfold(0 to 1, { (curr, next) ->
        Some(curr to (next to (curr + next)))
    })

    fun from(n: Int): Stream<Int> = Stream.unfold(n, { a -> Some(a to (a + 1)) })


    fun <A> constant(n: A): Stream<A> = Stream.unfold(n, { a -> Some(a to a) })


    fun ones(): Stream<Int> = Stream.unfold(1, { Some(1 to 1) })


fun <A, B> Stream<A>.map(f: (A) -> B): Stream<B> =
    Stream.unfold(this) { s: Stream<A> ->
        when (s) {
            is Cons -> Some(f(s.head()) to s.tail())
            else -> None
        }
    }

fun <A> Stream<A>.take(n: Int): Stream<A> =
    Stream.unfold(this) { s: Stream<A> ->
        when (s) {
            is Cons ->
                if (n > 0)
                    Some(s.head() to s.tail().take(n - 1))
                else None
            else -> None
        }
    }

fun <A> Stream<A>.takeWhile(p: (A) -> Boolean): Stream<A> =
    Stream.unfold(this,
        { s: Stream<A> ->
            when (s) {
                is Cons ->
                    if (p(s.head()))
                        Some(s.head() to s.tail())
                    else None
                else -> None
            }
        })

fun <A, B, C> Stream<A>.zipWith(
    that: Stream<B>,
    f: (A, B) -> C
): Stream<C> =
    Stream.unfold(this to that) { (ths: Stream<A>, tht: Stream<B>) ->
        when (ths) {
            is Cons ->
                when (tht) {
                    is Cons ->
                        Some(
                            Pair(
                                f(ths.head(), tht.head()),
                                ths.tail() to tht.tail()
                            )
                        )
                    else -> None
                }
            else -> None
        }
    }
fun <A, B> Stream<A>.zipAll(
    that: Stream<B>
): Stream<Pair<Option<A>, Option<B>>> =
    Stream.unfold(this to that) { (ths, tht) ->
        when (ths) {
            is Cons -> when (tht) {
                is Cons ->
                    Some(
                        Pair(
                            Some(ths.head()) to Some(tht.head()),
                            ths.tail() to tht.tail()
                        )
                    )
                else ->
                    Some(
                        Pair(
                            Some(ths.head()) to None,
                            ths.tail() to Stream.empty<B>()
                        )
                    )
            }
            else -> when (tht) {
                is Cons ->
                    Some(
                        Pair(
                            None to Some(tht.head()),
                            Stream.empty<A>() to tht.tail()
                        )
                    )
                else -> None
            }
        }
    }


    fun <A> Stream<A>.startsWith(that: Stream<A>): Boolean =
        this.zipAll(that)
            .takeWhile { !it.second.isEmpty() }
            .forAll { it.first == it.second }



    fun <A> Stream<A>.tails(): Stream<Stream<A>> =
        Stream.unfold(this) { s: Stream<A> ->
            when (s) {
                is Cons ->
                    Some(s to s.tail())
                else -> None
            }
        }


    fun <A, B> List<A>.map(f: (A) -> B): List<B> = when (this) {
        is ConsL -> ConsL(f(this.head), this.tail.map(f))
        is NilL -> NilL
    }


    fun <A, B> Stream<A>.scanRight(z: B, f: (A, () -> B) -> B): Stream<B> =
        foldRight({ z to Stream.of(z) },
            { a: A, p0: () -> Pair<B, Stream<B>> ->
                val p1: Pair<B, Stream<B>> by lazy { p0() }
                val b2: B = f(a) { p1.first }
                Pair<B, Stream<B>>(b2, Stream.cons({ b2 }, { p1.second }))
            }).second
