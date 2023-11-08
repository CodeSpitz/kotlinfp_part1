package com.example.fp_practice_with_kotlin.chapter6

import com.example.fp_practice_with_kotlin.chapter3.FList
import com.example.fp_practice_with_kotlin.chapter3.append
import com.example.fp_practice_with_kotlin.chapter3.foldRight
import com.example.fp_practice_with_kotlin.chapter4.FOption
import com.example.fp_practice_with_kotlin.chapter4.combine
import com.example.fp_practice_with_kotlin.chapter4.traverse
import org.junit.Assert
import org.junit.Test

/* RandomNumberGenerator */
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

val RNG.nonNegativeInt: Pair<Int, RNG> get()  {
    val (n, nextRNG) = this.nextInt()
    return (if (n < 0) -(n + 1) else n) to nextRNG
}
val RNG.double: Pair<Double, RNG> get() {
    val (n, nextRNG) = nonNegativeInt
    return n / (Int.MAX_VALUE.toDouble() + 1) to nextRNG
}
val RNG.intDouble: Pair<Pair<Int, Double>, RNG> get() {
    val (i, nextRNG1) = nonNegativeInt
    val (d, nextRNG2) = nextRNG1.double
    return (i to d) to nextRNG2
}
val RNG.doubleInt: Pair<Pair<Double, Int>, RNG> get() {
    val (gen, nextRNG) = intDouble
    return ((gen.second to gen.first) to nextRNG)
}

//fun double3(rng: RNG): Pair<Triple<Double, Double, Double>, RNG> {
//    val (generated1, rng1) = double(rng)
//
//}

fun RNG.ints(count: Int): Pair<FList<Int>, RNG> =
    if (count > 0) {
        val (i, nextRNG1) = nextInt()
        val (list, nextRNG2) = nextRNG1.ints(count - 1)
        FList(i).append(list) to nextRNG2
    } else {
        FList<Int>() to this
    }

typealias Rand<T> = (RNG) -> Pair<T, RNG>

val intR: Rand<Int> = { = it.nextInt() }
fun <T> unit(t: T): Rand<T> = { t to it }
fun <A: Any, B: Any> Rand<A>.map(block: (A) -> B): Rand<B> = { rng ->
    val (a, nextRNG) = this(rng)
    block(a)to nextRNG
}
fun nonNegativeEven(): Rand<Int> = { rng: RNG -> rng.nonNegativeInt }.map { it - (it % 2) }
val doubleR: Rand<Double> =  { rng: RNG -> rng.nonNegativeInt }.map { it / (Int.MAX_VALUE.toDouble() + 1) }
fun <A: Any, B: Any, C: Any> Rand<A>.map2(rb: Rand<B>, combine: (A, B) -> C): Rand<C> = { rng ->
    val (a, rng1) = this(rng)  // 첫 번째 Rand를 평가하여 결과와 새로운 RNG 상태를 얻는다
    val (b, rng2) = rb(rng1)   // rng1 상태를 사용하여 두 번째 Rand를 평가한가
    val c = combine(a, b)      // 두 결과를 combine으로 조합
    Pair(c, rng2)      // 새로운 값을 최종 RNG 상태와 함께 반환
}
fun <A: Any, B: Any> Rand<A>.both(rb: Rand<B>): Rand<Pair<A, B>> =
    map2(rb) { a, b -> a to b }
val intDoubleR: Rand<Pair<Int, Double>> = intR.both(doubleR)
val doubleIntR: Rand<Pair<Double, Int>> = doubleR.both(intR)

inline fun <A: Any, TO: Any> FList<Rand<A>>.traverse3(crossinline block: (Rand<A>) -> Rand<TO>): Rand<FList<TO>> =
    foldRight(unit(FList())) { curr, acc ->
        block(curr).map2(acc) { head, tail -> FList.Cons(head, tail) }
    }
inline fun <A: Any> FList<Rand<A>>.sequence3(): Rand<FList<A>> =
    traverse3 { it -> it }

fun nonNegativeLessThan(n: Int): Rand<Int>
//= { rng: RNG ->
//    val (i, rng2) = rng.nonNegativeInt
//    val mod = i % n
//    if (i + (n - 1) - mod >= 0) mod to rng2
//    else nonNegativeLessThan(n)(rng2)
//}
= { rng: RNG -> rng.nonNegativeInt }.flatMap {
    val mod = it % n
    if (it + (n - 1) - mod >= 0) unit(mod)
    else nonNegativeLessThan(n)
}
fun <A: Any, B: Any> Rand<A>.flatMap(block: (A) -> Rand<B>): Rand<B> = { rng: RNG ->
    val (a, rng2) = this(rng)
    block(a)(rng2)
}
fun <A: Any, B: Any> Rand<A>.mapF(block: (A) -> B): Rand<B> = flatMap { unit(block(it)) }
fun <A: Any, B: Any, C: Any> Rand<A>.map2F(rb: Rand<B>, block: (A, B) -> C): Rand<C>
    = flatMap { a -> rb.map { block(a, it) } }
fun rollDie(): Rand<Int> = nonNegativeLessThan(6)
fun rollDieFix(): Rand<Int> = nonNegativeLessThan(6).map { it + 1 }


// 여긴 그냥 타이핑
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
        fun <S, A> squence(fs: FList<State<S, A>>): State<S, List<A>> =
            foldRight(fs, unit(FList.empt<A>()), { f, acc -> map2(f, acc) { h, t -> Cons(h, t) } })
        fun <B> map:(f: (A) -> B): State<S, B> = flatMap { a -> unit<S, B>(f(a)) }
        fun <B> flatMap(f: (A) -> State<S, B>): State<S, B> =
            State { s: S ->
                val (a: A, s2: S) = this.run(s)
                f(a).run(s2)
            }
    }
}
typealias Rand2<A> = State<RNG, A>

class Chapter6 {
    @Test
    fun main() {
        val rng = SimpleRNG(1)

        println(rng.nonNegativeInt)
        println(rng.nonNegativeInt) // random인데 상태가 저장된다!
        println(rng.nonNegativeInt.second.nonNegativeInt)   // rng의 next rng의 생성값도 똑같다!
        println(rng.nonNegativeInt.second.nonNegativeInt)
        val ints = rng.ints(2)
        println(ints.first)
        println(ints.second.ints(5).first)
        println(rng.ints(6).first)  // seed가 같으면 계속 같은 값이 유지된다.

        println(rng.double)
        println(doubleR(rng))
        Assert.assertEquals(rng.double, doubleR(rng))
        println(doubleIntR(rng))

        val randList = FList<Rand<Int>>(unit(1), unit(2), unit(3), unit(4)).sequence3()
        print(randList(rng).first)  // 일반 list
        print(randList(rng).second) // rnd seed 1

        println(rollDieFix()(rng))

        Assert.assertEquals(listOf(1, 2, 3, 4), listOf(1, 2, 3, 4))
    }
}