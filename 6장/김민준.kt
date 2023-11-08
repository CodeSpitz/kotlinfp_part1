fun nonNegativeInt(rng: RNG): Pair<Int, RNG> {
    val (i1, rng2) = rng.nextInt()
    return (if (i1 < 0) -(i1 + 1) else i1) to rng2
}

fun double(rng: RNG): Pair<Double, RNG> {
    val (i, rng2) = nonNegativeInt(rng)
    return (i / (Int.MAX_VALUE.toDouble() + 1)) to rng2
}

fun intDouble(rng: RNG): Pair<Pair<Int, Double>, RNG> {
    val (i, rng2) = rng.nextInt()
    val (d, rng3) = double(rng2)
    return (i to d) to rng3
}

fun doubleInt(rng: RNG): Pair<Pair<Double, Int>, RNG> {
    val (id, rng2) = intDouble(rng)
    val (i, d) = id
    return (d to i) to rng2
}

fun double3(rng: RNG): Pair<Triple<Double, Double, Double>, RNG> {
    val doubleRand = doubleR()
    val (dl, rng2) = doubleRand(rng)
    val (d2, rng3) = doubleRand(rng2)
    val (d3, rng4) = doubleRand(rng3)
    return Triple(dl, d2, d3) to rng4
}

fun ints(count: Int, rng: RNG): Pair<List<Int>, RNG> =
        if (count > 0) {
            val (i, rl) = rng.nextInt()
            val (xs, r2) = ints(count - 1, rl)
            Cons(i, xs) to r2
        } else Nil to rng

fun doubleR(): Rand<Double> =
        map(::nonNegativeInt) { i ->
            i / (Int.MAX_VALUE.toDouble() + 1)
        }

fun <A, B, C> map2(ra: Rand<A>, rb: Rand<B>, f: (A, B) -> C): Rand<C> =
        { r1: RNG ->
            val (a, r2) = ra(r1)
            val (b, r3) = rb(r2)
            f(a, b) to r3
        }

fun <A> sequence(fs: List<Rand<A>>): Rand<List<A>> = {
    rng - >
    when (fs) {
        is Nil -> unit(List.empty<A>())(rng)
        is Cons -> {
            val (a, nrng) = fs.head(rng)
            val (xa, frng) = sequence(fs.tail)(nrng)
            Cons(a, xa) to frng
        }
    }
}

fun <A, B> flatMap(f: Rand<A>, g: (A) -> Rand<B>): Rand<B> = { rng ->
    val (a, rng2) = f(rng)
    g(a)(rng2)
}

fun nonNegativeIntLessThan(n: Int): Rand<Int> =
        flatMap(::nonNegativeInt) { i ->
            val mod = i % n
            if (i + (n - 1) - mod >= 0) unit(mod)
            else nonNegativeIntLessThan(n)
        }

fun <A, B> mapF(ra: Rand<A>, f: (A) -> B): Rand<B> = flatMap(ra) { a -> unit(f(a)) }

data class State<S, out A>(val run: (S) -> Pair<A, S>) {

    companion object {
        fun <S, A> unit(a: A): State<S, A> = State { s: S -> a to s }

        fun <S, A, B, C> map2(
                ra: State<S, A>,
                rb: State<S, B>,
                f: (A, B) -> C
        ): State<S, C> = ra.flatMap { a ->
            rb.map { b ->
                f(a, b)
            }
        }

        fun <S, A> sequence(fs: List<State<S, A>>): State<S, List<A>> =
                foldRight(fs, unit(List.empty<A>()), { f, acc ->
                    map2(f, acc) { h, t -> Cons(h, t) }
                })
    }

    fun <B> map(f: (A) -> B): State<S, B> = flatMap { a -> unit<S, B>(f(a)) }
    fun <B> flatMap(f: (A) -> State<S, B>): State<S, B> = State { s: S ->
        val (a: A, s2: S) = this.run(s)
        f(a).run(s2)
    }
}
