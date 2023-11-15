interface RNG {
    fun nextInt(): Pair<Int, RNG>
}

data class SimpleRNG(val seed: Long): RNG {
    override fun nextInt(): Pair<Int, RNG> {
        val newSeed = (seed * 0x5DEECE66DL + 0xBL) and 0xFFFFFFFFFFFFL
        val nextRNG = SimpleRNG(newSeed)
        val n = (newSeed ushr 16).toInt()
        return n to nextRNG
    }
}

interface StateActionSequencer {
    fun nextInt(): Pair<Int, StateActionSequencer>
    fun nextDouble(): Pair<Double, StateActionSequencer>
}

// ex. 6.1
fun nonNegativeInt(rng: RNG): Pair<Int, RNG> {
    val (intValue, nextRng) = rng.nextInt() ;
    return intValue shl 1 shr 1 to nextRng
}

// ex. 6.2
fun double(rng: RNG): Pair<Double, RNG> {
    val (numerator, rng2) = nonNegativeInt(rng);

    return numerator / (Int.MAX_VALUE.toDouble() + 1) to rng2
}

// ex. 6.3
fun intDouble(rng: RNG): Pair<Pair<Int, Double>, RNG> {
    val (intValue, rng2) = rng.nextInt();
    val (doubleValue, rng3) = double(rng2);

    return Pair(Pair(intValue, doubleValue), rng3)
}

fun doubleInt(rng: RNG): Pair<Pair<Double, Int>, RNG> {
    val (intValue, rng2) = rng.nextInt();
    val (doubleValue, rng3) = double(rng2);

    return Pair(Pair(doubleValue, intValue), rng3)
}

fun double3(rng: RNG): Pair<Triple<Double, Double, Double>, RNG> {
    val (doubleValue1, rng2) = double(rng)
    val (doubleValue2, rng3) = double(rng2)
    val (doubleValue3, rng4) = double(rng3)

    return Pair(Triple(doubleValue1, doubleValue2, doubleValue3), rng4)
}

// ex. 6.4
fun ints(count: Int, rng: RNG): Pair<List<Int>, RNG> {
    if (count > 0) {
        val (intValue, rng2) = rng.nextInt()
        val (xs, rng3) = ints(count - 1, rng2)
        Cons(intValue, xs) to rng3
    } else Nil to rng
}

typealias Rand<A> = (RNG) -> Pair<A, RNG>

fun <A> unit(a: A): Rand<A> = { rng -> a to rng }

fun <A, B> map(s: Rand<A>, f: (A) -> B): Rand<B> = {
    rng ->
        val (a, rng2) = s(rng)
        f(a) to rng2
}

// ex. 6.5
fun doubleR(): Rand<Double> =
    map(::nonNegativeInt) {
        i -> i / (Int.MAX_VALUE.toDouble() + 1)
    }

// ex. 6.6
fun <A, B, C> map2(
    ra: Rand<A>,
    rb: Rand<B>,
    f: (A, B) -> C
): Rand<C> = {
    r1: RNG ->
        val (a, r2) = ra(r1)
        val (b, r3) = rb(r2)
        f(a, b) to r3
}

// ex. 6.7
fun <A> sequence(fs: List<Rand<A>>): Rand<List<A>> = {
    rng -> when (fs) {
        is Nil -> unit(List.empty<A>())(rng)
        is Cons -> {
            val (a, nrng) = fs.head(rng)
            val (xa, frng) = sequence(fs.tail)(nrng)
            Cons(a, xa) to frng
        }
    }
}

// ex. 6.8
fun <A, B> flatMap(f: Rand<A>, g: (A) -> Rand<B>): Rand<B> =
    {
        val (a, rng2) = f(it)
        g(a)(rng2)
    }

fun nonNegativeIntLessThan(n: Int): Rand<Int> =
    flatMap(::nonNegativeInt) {
        i ->
            val mod = i % n
            if (i + (n - 1) - mod >= 0) unit(mod)
            else nonNegativeIntLessThan(n)
    }

// 6.9
fun <ITEM, RESULT> mapF(ra: Rand<ITEM>, f: (ITEM) -> RESULT): Rand<RESULT> =
    flatMap(ra) { a -> unit(f(a)) }

fun <ITEM, ITEM2, RESULT> map2F(
    ra: Rand<ITEM>,
    rb: Rand<ITEM2>,
    f: (ITEM, ITEM2) -> RESULT
): Rand<RESULT> =
    flatMap(ra) { a ->
        map(rb) { b ->
            f(a, b)
        }
    }

// 6.10
data class State<S, out A>(val run: (S) -> Pair<A, S>) {
    companion object {
        fun <S, A> unit(a: A): State<S, A> = State { s: S -> a to s }
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
                { f, ace ->
                    map2(f, ace) { h, t -> Cons(h, t) }
                }
            )
    }

    fun <B> map(f: (A) -> B): State<S, B> = flatMap { a -> unit<S, B>(f(a)) }
    fun <B> flatMap(f: (A) -> State<S, B>): State<S, B> =
        State { s: S ->
            val (a: A, s2: S) = this.run(s)
            f(a).run(s2)
        }
}

// 6.11
val update: (Input) -> (Machine) -> Machine = { i: Input ->
    { s: Machine ->
        when (i) {
            is Coin ->
                if (!s.locked || s.candies == 0) s
                else Machine(false, s.candies, s.coins + 1)
            is Turn ->
                if (s.locked || s.candies == 0) s
                else Machine(true, s.candies - 1, s.coins)
        }
    }
}

fun simulateMachine(
    inputs: List<Input>
): State<Machine, Tuple2<Int, Int>> =
    State.fx(Id.monad()) {
        inputs
            .map(update)
            .map(StateApi::modify)
            .stateSequential()
            .bind()
        val s = StateApi.get<Machine>().bind()
        Tuple2(s.candies, s.coins)
    }
