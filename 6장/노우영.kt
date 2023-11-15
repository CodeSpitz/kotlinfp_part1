package chapter6

class Chapter6 {
}

interface RNG {
    fun nextInt(): Pair<Int, RNG>
}

data class SimpleRNG(val seed: Long) : RNG {
    override fun nextInt(): Pair<Int, RNG> {
        val newSeed = (seed * 0x5DEECE66DL + 0xBL) and 0xFFFFFFFFFFFFL
        val nextRNG = SimpleRNG(newSeed)
        val n = (newSeed ushr 16).toInt()
        return Pair(n, nextRNG)
    }
}


//6.1
fun nonNegativeInt(rng: RNG): Pair<Int, RNG> {
    val (i, r) = rng.nextInt()
    return if (i < 0) Pair(-(i + 1), r) else Pair(i, r)
}

//6.2
fun double(rng: RNG): Pair<Double, RNG> {
    val (i, r) = nonNegativeInt(rng)
    return Pair(i / (Int.MAX_VALUE.toDouble() + 1), r)
}

//6.3
fun intDouble(rng: RNG): Pair<Pair<Int, Double>, RNG> {
    val (i, r1) = rng.nextInt()
    val (d, r2) = double(r1)
    return Pair(Pair(i, d), r2)
}

fun doubleInt(rng: RNG): Pair<Pair<Double, Int>, RNG> {
    val (d, r1) = double(rng)
    val (i, r2) = r1.nextInt()
    return Pair(Pair(d, i), r2)
}

fun double3(rng: RNG): Pair<Triple<Double, Double, Double>, RNG> {
    val (d1, r1) = double(rng)
    val (d2, r2) = double(r1)
    val (d3, r3) = double(r2)
    return Pair(Triple(d1, d2, d3), r3)
}

//6.4
fun ints(count: Int, rng: RNG): Pair<List<Int>, RNG> {
    tailrec fun go(count: Int, r: RNG, xs: List<Int>): Pair<List<Int>, RNG> =
        if (count == 0) Pair(xs, r)
        else {
            val (x, r2) = r.nextInt()
            go(count - 1, r2, xs + x)
        }
    return go(count, rng, listOf())
}

typealias Rand<A> = (RNG) -> Pair<A, RNG>

val intR: Rand<Int> = { rng -> rng.nextInt() }

fun <A> unit(a: A): Rand<A> = { rng -> Pair(a, rng) }

fun <A, B> map(s: Rand<A>, f: (A) -> B): Rand<B> = { rng ->
    val (a, r1) = s(rng)
    Pair(f(a), r1)
}

//6.5
fun doubleR(): Rand<Double> = map(::nonNegativeInt) { it / (Int.MAX_VALUE.toDouble() + 1) }

//6.6
fun <A, B, C> map2(
    ra: Rand<A>,
    rb: Rand<B>,
    f: (A, B) -> C,
): Rand<C> = { rng ->
    val (a, r1) = ra(rng)
    val (b, r2) = rb(r1)
    Pair(f(a, b), r2)
}

//6.7
fun <A> sequence(fs: List<Rand<A>>): Rand<List<A>> = { rng ->
    fs.foldRight(Pair(listOf<A>(), rng)) { f, acc ->
        val (a, r) = f(acc.second)
        Pair(acc.first + a, r)
    }
}

fun nonNegativeLessThan(n: Int): Rand<Int> = { rng ->
    val (i, r) = nonNegativeInt(rng)
    val mod = i % n
    if (i + (n - 1) - mod >= 0) Pair(mod, r)
    else nonNegativeLessThan(n)(r)
}

//6.8
fun <A, B> flatMap(f: Rand<A>, g: (A) -> Rand<B>): Rand<B> = { rng ->
    val (a, r1) = f(rng)
    g(a)(r1)
}

//6.9
fun <A, B> mapF(s: Rand<A>, f: (A) -> B): Rand<B> = flatMap(s) { a -> unit(f(a)) }

fun <A, B, C> map2F(
    ra: Rand<A>,
    rb: Rand<B>,
    f: (A, B) -> C,
): Rand<C> = flatMap(ra) { a -> map(rb) { b -> f(a, b) } }

fun rollDie(): Rand<Int> = nonNegativeLessThan(6)

//typealias State<S, A> = (S) -> Pair<A, S>
//
data class StateC6<S, out A> (val run: (S) -> Pair<A, S>)

//typealias Rand<A> = State<RNG, A>

//6.10
fun <S, A> unitS(a: A): StateC6<S, A> = StateC6 { Pair(a, it) }
fun <S, A, B> mapS(s: StateC6<S, A>, f: (A) -> B): StateC6<S, B> = StateC6 { s.run(it).let { Pair(f(it.first), it.second) } }
fun <S, A, B> flatMapS(s: StateC6<S, A>, f: (A) -> StateC6<S, B>): StateC6<S, B> = StateC6 { s.run(it).let { f(it.first).run(it.second) } }
fun <S, A>sequenceS(fs: List<StateC6<S, A>>): StateC6<S, List<A>> = StateC6 { fs.foldRight(Pair(listOf<A>(), it)) { f, acc -> f.run(acc.second).let { Pair(acc.first + it.first, it.second) } } }

//6.11
sealed class Input
object Coin : Input()
object Turn : Input()

data class Machine(val locked: Boolean, val candies: Int, val coins: Int)

fun simulateMachine(inputs: List<Input>): StateC6<Machine, Pair<Int, Int>> = StateC6 { m ->
    inputs.foldRight(Pair(m.coins, m.candies)) { i, acc ->
        when (i) {
            is Coin -> {
                if (acc.second > 0 && acc.first >= 0) Pair(acc.first + 1, acc.second)
                else acc
            }
            is Turn -> {
                if (acc.second > 0 && acc.first > 0) Pair(acc.first - 1, acc.second - 1)
                else acc
            }
        }
    }.let { Pair(it, m) }
}
