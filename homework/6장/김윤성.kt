package playground

interface RNG {
    fun nextInt(): Pair<Int, RNG>
}

data class SimpleRNG(val seed: Long) : RNG {
    override fun nextInt(): Pair<Int, RNG> {
        val newSeed =
            (seed * 0x5DEECE66DL + 0xBL) and
                    0xFFFFFFFFFFFFL
        val nextRNG = SimpleRNG(newSeed)
        val n = (newSeed ushr 16).toInt()
        return n to nextRNG
    }
}

fun nonNegativeInt(rng: RNG): Pair<Int, RNG> {
    val (i, range) = rng.nextInt()
    return Pair(
        Int.MAX_VALUE.shr(1) + ceil(i.ushr(1).toDouble()).toInt(),
        range
    )
}

fun double(rng: RNG): Pair<Double, RNG> {
    val (i, range) = nonNegativeInt(rng)
    return (i / (Int.MAX_VALUE.toDouble() + 1)) to range
}

fun intDouble(rng: RNG): Pair<Pair<Int, Double>, RNG> {
    val (i, intRange) = rng.nextInt()
    val (d, rng3) = double(rng)
    return i to d to rng3
}


fun doubleInt(rng: RNG): Pair<Pair<Double, Int>, RNG> {
    val (id, rng2) = intDouble(rng)
    val (i, d) = id
    return (d to i) to rng2
}

fun ints(count: Int, rng: RNG): Pair<List<Int>, RNG> =
    if (count > 0) {
        val (i, range1) = rng.nextInt()
        val (xs, range2) = ints(count - 1, range1)
        Cons(i, xs) to range2
    } else Nil to rng

fun doubleR(): Rand<Double> =
    map(::nonNegativeInt) { i ->
        i / (Int.MAX_VALUE.toDouble() + 1)
    }

fun <A, B, C> map(ra: Rand<A>, rb: Rand<B>, f: (A, B) -> C): Rand<C> =
    { r1: RNG ->
        val (a, r2) = ra(r1)
        val (b, r3) = rb(r2)
        f(a, b) to r3
    }

fun <A> sequence(fs: List<Rand<A>>): Rand<List<A>> = { rng ->
    when (fs) {
        is Nil -> unit(List.empty<A>())(rng)
        is Cons -> {
            val (a, nrng) = fs.head(rng)
            val (xa, frng) = sequence(fs.tail)(nrng)
            Cons(a, xa) to frng
        }
    }
}

fun <A, B> flatMap(f: Rand<A>, g: (A) -> Rand<B>): Rand<B> =
    { rng ->
        val (a, range) = f(rng)
        g(a)(range)
    }

fun nonNegativeIntLessThan(n: Int): Rand<Int> =
    flatMap(::nonNegativeInt) { i ->
        val mod = i % n
        if (i + (n - 1) - mod >= 0) unit(mod)
        else nonNegativeIntLessThan(n)
    }

fun <A, B> mapF(ra: Rand<A>, f: (A) -> B): Rand<B> =
    flatMap(ra) { a -> unit(f(a)) }
fun <A, B, C> map2F(
    ra: Rand<A>,
    rb: Rand<B>,
    f: (A, B) -> C
): Rand<C> =
    flatMap(ra) { a ->
        map(rb) { b ->
            f(a, b)
        }
    }

data class State<S, out A>(val run: (S) -> Pair<A, S>) {

    companion object {
        fun <S, A> unit(a: A): State<S, A> =
            State { s: S -> a to s }

        fun <S, A, B, C> map2(
            ra: State<S, A>,
            rb: State<S, B>,
            f: (A, B) -> C
        ): State<S, C> =
            ra.flatMap { a ->
                rb.map { b ->
                    f(a, b)
                }
            }

        fun <S, A> sequence(fs: List<State<S, A>>): State<S, List<A>> =
            foldRight(fs, unit(List.empty<A>()),
                { f, acc ->
                    map2(f, acc) { h, t -> Cons(h, t) }
                }
            )
    }

    fun <B> map(f: (A) -> B): State<S, B> =
        flatMap { a -> unit<S, B>(f(a)) }

    fun <B> flatMap(f: (A) -> State<S, B>): State<S, B> =
        State { s: S ->
            val (a: A, s2: S) = this.run(s)
            f(a).run(s2)
        }
}