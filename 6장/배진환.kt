package fpkotlin.practice

import kotlin.math.*

interface RNG {
    fun nextInt(): Pair<Int, RNG>
}

data class SimpleRNG(val seed: Long) : RNG {
    override fun nextInt(): Pair<Int, RNG> {
        val newSeed =
            (seed * 0x5DEECE66DL + 0xBL) and
                0xFFFFFFFFFFFFL // <1>
        val nextRNG = SimpleRNG(newSeed) // <2>
        val n = (newSeed ushr 16).toInt() // <3>
        return n to nextRNG // <4>
    }
}

// 1
fun nonNegativeInt(rng: RNG): Pair<Int, RNG> {
    val (i, rng2) = rng.nextInt()
    return Pair(
        Int.MAX_VALUE.shr(1) + ceil(i.ushr(1).toDouble()).toInt(),
        rng2
    )
}

// 2
fun double(rng: RNG): Pair<Double, RNG> {
    val (i, rng2) = nonNegativeInt(rng)

    return Pair(
        i / Int.MAX_VALUE.toDouble(),
        rng
    )
}

// 3
fun intDouble(rng: RNG): Pair<Pair<Int, Double>, RNG> {
    val (i, rng2) = rng.nextInt()
    val (d, rng3) = double(rng)
    return i to d to rng3
}

fun doubleInt(rng: RNG): Pair<Pair<Double, Int>, RNG> {
    val (d, rng2) = double(rng)
    val (i, rng3) = rng2.nextInt()
    return d to i to rng3
}

fun double3(rng: RNG): Pair<Triple<Double, Double, Double>, RNG> {
    val (d1, rng2) = double(rng)
    val (d2, rng3) = double(rng)
    val (d3, rng4) = double(rng)
    return Triple(d1, d2, d3) to rng4
}

// 4
private tailrec fun makeRNGList(
    count: Int,
    rng: RNG,
    l: FList<Int> = FList()
): Pair<FList<Int>, RNG> {
    return if (count == 0) l.reverse() to rng else {
        val (i, rng2) = rng.nextInt()
        makeRNGList(count - 1, rng2, FList(i, l))
    }
}

fun ints(count: Int, rng: RNG): Pair<FList<Int>, RNG> =
    makeRNGList(count, rng)

// 5
typealias Rand<ITEM> = (RNG) -> Pair<ITEM, RNG>

val intR: Rand<Int> = { rng -> rng.nextInt() }
fun <ITEM> unit(e: ITEM): Rand<ITEM> = { rng -> e to rng }
fun <ITEM, OTHER> map(
    rand: Rand<ITEM>,
    block: (ITEM) -> OTHER
): Rand<OTHER> = {
    val (e1, rng) = rand(it)
    block(e1) to rng
}

fun double2(rng: RNG): Rand<Double> = map(::nonNegativeInt) {
    it / Int.MAX_VALUE.toDouble()
}

// 6
fun <I1, I2, RETURN> map2(
    rand1: Rand<I1>,
    rand2: Rand<I2>,
    block: (I1, I2) -> RETURN
): Rand<RETURN> = { rng ->
    val (i1, rng1) = rand1(rng)
    val (i2, rng2) = rand2(rng1)
    Pair(
        block(i1, i2), rng2
    )
}

// 7
fun <ITEM> sequence(l: FList<Rand<ITEM>>): Rand<FList<ITEM>> = {
    l.fold(FList<ITEM>() to it) { (list, rng), rand ->
        val (item, rng2) = rand(rng)
        FList(item, list) to rng2
    }.let { (list, rng) ->
        list.reverse() to rng
    }
}

// 8
fun <ITEM, OTHER> flatMap(
    rand: Rand<ITEM>,
    block: (ITEM) -> Rand<OTHER>
): Rand<OTHER> = { rng ->
    rand(rng).let { (item, rng) -> block(item)(rng) }
}

// 9
fun <ITEM, OTHER> mapF(
    rand: Rand<ITEM>,
    block: (ITEM) -> OTHER
): Rand<OTHER> = {
    val (i, rng) = rand(it)
    block(i) to rng
}

fun <S, I1, I2, R> map2F(
    r1: Rand<I1>,
    r2: Rand<I2>,
    block: (I1, I2) -> R
): Rand<R> = flatMap(r1) { i1 ->
    map(r2) { i2 ->
        block(i1, i2)
    }
}

// 10
data class State<S, out ITEM>(val run: (S) -> Pair<ITEM, S>) {
    companion object {
        fun <S, ITEM> unit(i: ITEM): State<S, ITEM> =
            State { s: S -> i to s }

        fun <S, I1, I2, R> map2(
            s1: State<S, I1>,
            s2: State<S, I2>,
            block: (I1, I2) -> R
        ): State<S, R> = s1.flatMap { i1 ->
            s2.map { i2 ->
                block(i1, i2)
            }
        }
    }

    fun <OTHER> map(block: (ITEM) -> OTHER): State<S, OTHER> = State {
        val (i, s) = run(it)
        block(i) to s
    }

    fun <OTHER> flatMap(block: (ITEM) -> State<S, OTHER>): State<S, OTHER> =
        State { s ->
            val (i, s2) = run(s)
            block(i).run(s2)
        }
}

// 11
sealed interface Input
object Coin : Input
object Turn : Input

data class Machine(val locked: Boolean, val coins: Int, val candies: Int)

fun simulateMachine(inputs: List<Input>): State<Machine, Pair<Int, Int>> = State{ machine ->
    inputs.fold(machine){ m,input ->
        when(input){
            is Coin ->
                if(!m.locked || m.candies ==0) m
                else Machine(false,m.coins+1,m.candies)
            is Turn ->
                if(m.locked || m.candies ==0) m
                else Machine(true,m.coins,m.candies-1)
        }
    }.let{
        it.candies to it.coins to it
    }

}
