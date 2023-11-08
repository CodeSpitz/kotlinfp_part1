import study.fp.FList.*
import study.fp.FList
import study.fp.*

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

/* 연습문제 6.1 */
fun nonNegativeInt(rng: RNG): Pair<Int, RNG> {
    val (i, rng2) = rng.nextInt()
    return (if (i < 0) -(i + 1) else i) to rng2
}

/* 연습문제 6.2 */
fun double(rng: RNG): Pair<Double, RNG> {
    val (i, rng2) = nonNegativeInt(rng)
    return (i / (Int.MAX_VALUE.toDouble() + 1)) to rng2
}

/* 연습문제 6.3 */
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
    val (d2, rng2) = double(rng)
    val (d3, rng3) = double(rng2)
    val (d4, rng4) = double(rng3)
    return Triple(d2, d3, d4) to rng4
}

/* 연습문제 6.4 */
fun ints(count: Int, rng: RNG): Pair<FList<Int>, RNG> =
    if (count > 0) {
        val (i, rng2) = rng.nextInt()
        val (list, rng3) = ints(count - 1, rng2)
        FList.Cons(i, list) to rng3
    } else {
        Nil to rng
    }


typealias Rand<A> = (RNG) -> Pair<A, RNG>
fun <A> unit(a: A): Rand<A> = { rng -> a to rng}
fun <A, B> map(s: Rand<A>, f: (A) -> B): Rand<B> = { rng ->
    val (a, rng2) = s(rng)
    f(a) to rng2
}

/* 연습문제 6.5 */
fun doubleR(): Rand<Double> =
    map({rng: RNG -> nonNegativeInt(rng)}) { i -> i / (Int.MAX_VALUE.toDouble() + 1)}

/* 연습문제 6.6 */
fun <A, B, C> map2(ra: Rand<A>, rb: Rand<B>, f: (A, B) -> C): Rand<C> = { r1: RNG ->
    val (a, r2) = ra(r1)
    val (b, r3) = rb(r2)
    f(a, b) to r3
}

/* 연습문제 6.7 */
fun <A> sequence(list: FList<Rand<A>>): Rand<FList<A>> = { rng ->
    when (list) {
        is FList.Nil -> unit(FList.emptyList<A>())(rng)
        is FList.Cons -> {
            val (a, nrng) = list.head(rng)
            val (xa, frng) = sequence(list.tail)(nrng)
            FList.Cons(a, xa) to frng
        }
    }
}
fun <A> sequence2(list: FList<Rand<A>>): Rand<FList<A>> =
    foldRight(list, unit(FList.emptyList())) { f, acc ->
        map2(f, acc) { h, t -> Cons(h, t) }
    }
fun ints2(count: Int, rng: RNG): Pair<FList<Int>, RNG> {
    fun go(c: Int): FList<Rand<Int>> =
        if (c == 0) Nil
        else Cons({ r -> 1 to r}, go(c - 1))
    return sequence2(go(count))(rng)
}

/* 연습문제 6.8 */
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

/* 연습문제 6.9 */
fun <A, B> mapF(ra: Rand<A>, f: (A) -> B): Rand<B> =
    flatMap(ra) { a -> unit(f(a)) }
fun <A, B, C> map2F(ra: Rand<A>, rb: Rand<B>, f: (A, B) -> C): Rand<C> =
    flatMap(ra) { a ->
        map(rb) { b ->
            f(a, b)
        }
    }

/* 연습문제 6.10 */
data class State<S, out A>(val run: (S) -> Pair<A, S>) {
    companion object {
        fun <S, A> unit(a: A): State<S, A> = State { s: S -> a to s }
        fun <S, A, B, C> map2(ra: State<S, A>, rb: State<S, B>, f: (A, B) -> C): State<S, C> =
            ra.flatMap { a ->
                rb.map { b ->
                    f(a, b)
                }
            }
        fun <S, A> sequence(fs: FList<State<S, A>>): State<S, FList<A>> =
            study.fp.foldRight(fs, unit(FList.emptyList())
            ) { f, acc ->
                map2(f, acc) { h, t -> Cons(h, t) } }
    }
    fun <B> map(f: (A) -> B): State<S, B> = flatMap { a -> unit<S, B>(f(a))}
    fun <B> flatMap(f: (A) -> State<S, B>): State<S, B> =
        State { s:S ->
            val (a: A, s2: S) = this.run(s)
            f(a).run(s2)
        }
}

/* 연습문제 6.11 */
sealed class Input
object Coin: Input()
object Turn: Input()
data class Machine(val locked: Boolean, val candies: Int, val coins: Int)

val update: (Input) -> (Machine) -> Machine =
    { i: Input ->
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