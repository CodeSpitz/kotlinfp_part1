
interface RNG {
    fun nextInt(): Pair<Int, RNG>
}

typealias Rand<A> = (RNG) -> Pair<A, RNG>

// 연습문제 6-1
fun nonNegativeInt(rng: RNG): Pair<Int, RNG> {
    val (i1, rng2) = rng.nextInt()
    return (if (i1 < 0) -(i1 + 1) else i1) to rng2
}

// 연습문제 6-2
fun double(rng: RNG): Pair<Double, RNG> {
    val (i, rng2) = nonNegativeInt(rng)
    return (i / (Int.MAX_VALUE.toDouble() + 1)) to rng2
}

// 연습문제 6-3
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
    val (d1, rng2) = doubleRand(rng)
    val (d2, rng3) = doubleRand(rng2)
    val (d3, rng4) = doubleRand(rng3)
    return Triple(d1, d2, d3) to rng4
}

// 연습문제 6-4
fun ints(count: Int, rng: RNG): Pair<List<Int>, RNG> =
    if (count > 0) {
        val (i, r1) = rng.nextInt()
        val (xs, r2) = ints(count - 1, r1)
        Cons(i, xs) to r2 }
    else
        Nil to rng

// 연습문제 6-5
fun doubleR(): Rand<Double> =
    map(::nonNegativeInt) { i -> i / (Int.MAX_VALUE.toDouble() + 1) }

// 연습문제 6-6
fun <A, B, C> map2(ra: Rand<A>, rb: Rand<B>, f: (A, B) -> C): Rand<C> = {
    r1: RNG -> val (a, r2) = ra(r1)
    val (b, r3) = rb(r2) f(a, b) to r3
}

// 연습문제 6-7
fun <A> sequence2(fs: List<Rand<A>>): Rand<List<A>> =
    foldRight(fs, unit(List.empty()), { f, acc -> map2(f, acc, { h, t -> Cons(h, t) }) })

fun ints2(count: Int, rng: RNG): Pair<List<Int>, RNG> {
    fun go(c: Int): List<Rand<Int>> = if (c == 0) Nil else Cons({ r -> 1 to r }, go(c - 1))
    return sequence2(go(count))(rng)
}


// 연습문제 6-8
fun <A, B> flatMap(f: Rand<A>, g: (A) -> Rand<B>): Rand<B> = { rng -> val (a, rng2) = f(rng) g(a)(rng2) }

fun nonNegativeIntLessThan(n: Int): Rand<Int> =
    flatMap(::nonNegativeInt) { i ->
        val mod = i % n
        if (i + (n - 1) - mod >= 0) unit(mod)
        else nonNegativeIntLessThan(n)
    }


// 연습문제 6-9
fun <A, B> mapF(ra: Rand<A>, f: (A) -> B): Rand<B> =
    flatMap(ra) { a -> unit(f(a)) }

fun <A, B, C> map2F( ra: Rand<A>, rb: Rand<B>,f: (A, B) -> C ): Rand<C> =
    flatMap(ra) { a -> map(rb) { b -> f(a, b) } }

// 연습문제 6-10
data class State<S, out A>(val run: (S) -> Pair<A, S>) {
    companion object {
        fun <S, A> unit(a: A): State<S, A> = State { s: S -> a to s }
        fun <S, A, B, C> map2( ra: State<S, A>, rb: State<S, B>, f: (A, B) -> C ): State<S, C> =
            ra.flatMap { a -> rb.map { b -> f(a, b) } }
        fun <S, A> sequence(fs: List<State<S, A>>): State<S, List<A>> =
            foldRight(fs, unit(List.empty<A>()), { f, acc -> map2(f, acc) { h, t -> Cons(h, t) } } )

    }

    fun <B> map(f: (A) -> B): State<S, B> =
        flatMap { a -> unit<S, B>(f(a)) }

    fun <B> flatMap(f: (A) -> State<S, B>): State<S, B> =
        State { s: S -> val (a: A, s2: S) = this.run(s) f(a).run(s2) }

}

// 연습문제 6-11
import arrow.core.Id
import arrow.core.Tuple2
import arrow.core.extensions.id.monad.monad
import arrow.mtl.State
import arrow.mtl.StateApi
import arrow.mtl.extensions.fx
import arrow.mtl.runS
import arrow.mtl.stateSequential

val update: (Input) -> (Machine) -> Machine =
    {
        i: Input -> { s: Machine ->
            when (i) {
                is Coin -> if (!s.locked || s.candies == 0) s
                            else Machine(false, s.candies, s.coins + 1)
                is Turn -> if (s.locked || s.candies == 0) s
                                    else Machine(true, s.candies - 1, s.coins)
                         }
            }
    }

fun simulateMachine( inputs: List<Input> ): State<Machine, Tuple2<Int, Int>> =
    State.fx(Id.monad()) {
        inputs .map(update) .map(StateApi::modify) .stateSequential() .bind()
        val s = StateApi.get<Machine>().bind()
        Tuple2(s.candies, s.coins)
    }

