package com.example.fp_practice_with_kotlin.chapter5

import com.example.fp_practice_with_kotlin.chapter3.FList
import com.example.fp_practice_with_kotlin.chapter4.FOption
import com.example.fp_practice_with_kotlin.chapter4.FOption.*
import com.example.fp_practice_with_kotlin.chapter4.getOfElse
import com.example.fp_practice_with_kotlin.chapter4.isEmpty
import com.example.fp_practice_with_kotlin.chapter4.map
import com.example.fp_practice_with_kotlin.chapter5.FStream.*
import com.example.fp_practice_with_kotlin.chapter5.FStream.Cons

import org.junit.Assert
import org.junit.Test

sealed class FStream<out VALUE: Any> {
    data class Cons<out VALUE: Any>internal constructor(
        val head: () -> VALUE,
        val tail: () -> FStream<VALUE>
    ): FStream<VALUE>()
    data object Empty: FStream<Nothing>()
    companion object{
        operator fun <VALUE: Any> invoke(
            head: () -> VALUE,
            tail: () -> FStream<VALUE>
        ): FStream<VALUE> = FStream.Cons(head, tail)
//        operator fun <VALUE: Any> invoke(
//            head: () -> VALUE,
//            tail: () -> FStream<VALUE>
//        ): FStream<VALUE> {
//            val _head: VALUE by lazy(head)
//            val _tail: FStream<VALUE> by lazy(tail)
//            return Cons({ _head }, { _tail })
//        }
//        inline operator fun <ITEM: Any> invoke(vararg items: ITEM): FList<ITEM>
        operator fun <VALUE: Any> invoke(
            vararg items: VALUE
        ): FStream<VALUE> {
            return if (items.isEmpty()) Empty
            else FStream(
                { items[0] },
                { FStream(items = items.sliceArray(1 until items.size)) }
            )
        }

        inline operator fun <VALUE: Any> invoke(): FStream<VALUE> = Empty
    }
}

val <VALUE: Any> FStream<VALUE>.headOption: FOption<VALUE> get() =
    when (this) {
        is Empty -> None
        is Cons -> Some(head())
    }

tailrec fun <ITEM:Any, ACC:Any> FStream<ITEM>._foldRight(base: () -> ACC, origin:(ITEM, () -> ACC) -> ACC, block: (() -> ACC) -> ACC): ACC
       = when (this) {
           is Cons -> when(tail()) {
               is Cons -> tail()._foldRight(base, origin) { acc -> block({ origin(head(), acc) }) }
               is Empty -> block({ origin(head(), base) })
           }
            is Empty -> base()
       }
fun <ITEM: Any, ACC: Any> FStream<ITEM>.foldRight(base: () -> ACC, origin: (ITEM, () -> ACC) -> ACC): ACC
    = _foldRight(base, origin) { it() }

fun <ITEM: Any, ACCU: Any> FStream<ITEM>.fold(base: () -> ACCU, block: (() -> ACCU, ITEM) -> ACCU): ACCU
        = when (this) {
            is Cons -> tail().fold({ block(base, head()) }, block)
            is Empty -> base()
        }


// ✏️[연습문제 5.1]
fun <ITEM: Any> FStream<ITEM>.toList(): List<ITEM>
    = fold({ listOf() }) { acc, it -> acc() + it }
// ✏️[연습문제 5.2]
fun <ITEM: Any> FStream<ITEM>.take(n: Int): FStream<ITEM>
    =  if (n > 0 && this is Cons) {  FStream(head, { tail().take(n - 1) }) } else { Empty }
fun <ITEM: Any> FStream<ITEM>.dropFirst(n: Int): FStream<ITEM>
        =  if (n > 0 && this is Cons) {  tail().dropFirst(n - 1) } else { this }
fun <ITEM: Any> FStream<ITEM>.dropLastOne(): FStream<ITEM>
     = when (this) {
        is Empty -> this
        is Cons -> if (tail() is Empty) Empty else Cons(head, { tail().dropLastOne() })
     }
fun <ITEM: Any> FStream<ITEM>.dropLast(n: Int): FStream<ITEM>
    = if (n > 0) dropLastOne().dropLast(n - 1) else this
// ✏️[연습문제 5.3]
fun <ITEM: Any> FStream<ITEM>.takeWhile(block: (ITEM) -> Boolean): FStream<ITEM>
    = if (this is Cons && block(head())) { FStream(head, { tail().takeWhile(block) }) } else { Empty }
// ✏️[연습문제 5.4]
fun <ITEM: Any> FStream<ITEM>.forAll(block: (ITEM) -> Boolean): Boolean
    = foldRight({ true }) { it, acc -> acc() && block(it) }
// ✏️[연습문제 5.5]
fun <ITEM: Any> FStream<ITEM>.takeWhile2(block: (ITEM) -> Boolean): FStream<ITEM>
    = foldRight({ FStream() }) { it, acc -> if (block(it)) FStream({ it }, acc) else acc() }
// ✏️[연습문제 5.6]
val <VALUE: Any> FStream<VALUE>.headOption2: FOption<VALUE> get() =
    foldRight({ FOption() }) { it, _ -> FOption(it) }
// ✏️[연습문제 5.7]
fun <ITEM: Any, TO: Any> FStream<ITEM>.map(block: (ITEM) -> TO): FStream<TO>
    = foldRight({ FStream() }) { it, acc -> FStream({ block(it) }, acc) }
fun <ITEM: Any> FStream<ITEM>.filter(block: (ITEM) -> Boolean): FStream<ITEM>
    = foldRight({ FStream() }) { it, acc -> if (block(it)) FStream({ it }, acc) else acc() }
fun <ITEM: Any> FStream<ITEM>.append(block: () -> FStream<ITEM>): FStream<ITEM>
    = foldRight(block) { it, acc -> FStream({ it }, acc) }
// ✏️[연습문제 5.8]
fun <ITEM: Any> constant(item: ITEM): FStream<ITEM>
    = FStream({ item }, { constant(item) })
// ✏️[연습문제 5.9]
fun from(n: Int): FStream<Int>
    = FStream({ n }, { from(n + 1) })
// ✏️[연습문제 5.10]
fun fibs(): FStream<Int> {
    fun go(curr: Int, next: Int): FStream<Int>
        = FStream({ curr }, { go(next, curr + next) })
    return go(0, 1)
}
// ✏️[연습문제 5.11]
fun <ITEM: Any, SEED: Any> unfold(seed: SEED, block: (SEED) -> FOption<Pair<ITEM, SEED>>): FStream<ITEM>
    = block(seed)
        .map { pair -> FStream({ pair.first }, {
            unfold(pair.second, block)
        }) }
        .getOfElse { Empty }
// ✏️[연습문제 5.12]
fun fibs2(): FStream<Int> = unfold(Pair(0, 1), { FOption(it.first to (it.second to it.first + it.second)) })
fun from2(n: Int): FStream<Int>
    = unfold(n) { FOption(it to it + 1) }
fun <ITEM: Any> constant2(item: ITEM): FStream<ITEM>
    = unfold(item) { FOption(it to it) }
fun ones2(): FStream<Int> = unfold(1) { FOption(it to it) }
// ✏️[연습문제 5.13]
fun <ITEM: Any, TO: Any> FStream<ITEM>.map2(block: (ITEM) -> TO): FStream<TO>
    = unfold(this) {
        when (it) {
            is Cons -> FOption(block(it.head()) to it.tail())
            is Empty -> FOption()
        }
    }
fun <ITEM: Any> FStream<ITEM>.take2(n: Int): FStream<ITEM>
    = unfold(this) {
        if (n > 0 && it is Cons) FOption(it.head() to it.tail().take2(n - 1) ) else FOption()
    }
fun <ITEM: Any> FStream<ITEM>.takeWhile3(block: (ITEM) -> Boolean): FStream<ITEM>
    = unfold(this) {
        if (it is Cons && block(it.head())) FOption(it.head() to it.tail().takeWhile3(block)) else FOption()
    }
fun <ITEM: Any, OTHER: Any, TO: Any> FStream<ITEM>.zipWith(that: FStream<OTHER>, block: (ITEM, OTHER) -> TO): FStream<TO>
    = unfold(this to that) { (ths: FStream<ITEM>, tht: FStream<OTHER>) ->
        if (ths is Cons && tht is Cons)
            FOption(block(ths.head(), tht.head()) to (ths.tail() to tht.tail()))
        else
            FOption()
    }
fun <ITEM: Any, OTHER: Any> FStream<ITEM>.zipAll(that: FStream<OTHER>): FStream<Pair<FOption<ITEM>, FOption<OTHER>>>
    = unfold(this to that) { (ths: FStream<ITEM>, tht: FStream<OTHER>) ->
        when (ths) {
            is Cons -> when (tht) {
                is Cons -> FOption(
                    Pair(
                        FOption(ths.head()) to FOption(tht.head()),
                        ths.tail() to tht.tail()
                    )
                )
                is Empty -> FOption(
                    Pair(
                        FOption(ths.head()) to FOption(),
                        ths.tail() to FStream()
                    )
                )
            }
            is Empty -> when (tht) {
                is Cons -> FOption(
                    Pair(
                        FOption<ITEM>() to FOption(tht.head()),
                        FStream<ITEM>() to tht.tail()
                    )
                )
                is Empty -> FOption()
            }
        }
    }
fun <ITEM: Any> FStream<ITEM>.startWith(that: FStream<ITEM>): Boolean
    = zipAll(that)
        .takeWhile3 { it.second.isEmpty.not() }
        .forAll { it.first == it.second }
fun <ITEM: Any> FStream<ITEM>.tails(): FStream<FStream<ITEM>>
    = unfold(this) { ths: FStream<ITEM> ->
        if (ths is Cons) FOption(ths to ths.tail()) else FOption()
    }

// 이건 정말 못풀겠다... GG
fun <ITEM: Any, OTHER: Any> FStream<ITEM>.scanRight(initResult: OTHER, block: (ITEM, OTHER) -> OTHER): FStream<OTHER>
    = foldRight({ initResult to FStream(initResult) }) { it, acc ->
        val acc = acc()
        val result = block(it, acc.first)
        result to FStream({ result }, { acc.second })
    }.second
class Chapter5_1 {
    @Test
    fun main() {
        val stream: FStream<Int> = FStream<Int>(1, 2, 3, 4)
        println(stream.foldRight({ 0 }) { it, acc -> acc() + it })
        println(stream.zipAll(FStream(1, 2, 3)).take(3).toList())
        stream.tails().toList().forEach { println(it.toList()) }

        Assert.assertEquals(listOf(1, 2, 3, 4), stream.toList())
        Assert.assertEquals(listOf(1, 2), stream.take(2).toList())
        Assert.assertEquals(listOf(1, 2), stream.takeWhile { it < 3 }.toList())
        Assert.assertEquals(stream.takeWhile { it < 3 }.toList(), stream.takeWhile2 { it < 3 }.toList())
        Assert.assertEquals(stream.takeWhile2 { it < 3 }.toList(), stream.takeWhile3 { it < 3 }.toList())
        Assert.assertEquals(listOf(3, 4), stream.dropFirst(2).toList())
        Assert.assertEquals(listOf(1, 2, 3), stream.dropLastOne().toList())
        Assert.assertEquals(listOf(1, 2, 3), stream.dropLast(1).toList())
        Assert.assertEquals(listOf(1, 2), stream.dropLast(2).toList())
        Assert.assertEquals(emptyList<Int>(), stream.dropLast(5).toList())
        Assert.assertEquals(FOption(1), stream.headOption2)
        Assert.assertEquals(listOf("1", "2", "3", "4"), stream.map { "$it" }.toList())
        Assert.assertEquals(listOf("1", "2", "3", "4"), stream.map2 { "$it" }.toList())
        Assert.assertEquals(listOf(2, 4), stream.filter { it % 2 == 0 }.toList())
        Assert.assertEquals(listOf(1, 2, 3, 4, 5, 6), stream.append { FStream(5, 6) }.toList())
        Assert.assertEquals(listOf(0, 1, 1, 2, 3, 5, 8), fibs2().take(7).toList())
        Assert.assertEquals(fibs2().take(7).toList(), fibs2().take2(7).toList())
        Assert.assertEquals(listOf(4, 5, 6, 7, 8, 9, 10), from2(4).take(7).toList())
        Assert.assertEquals(listOf(2, 4, 6), stream.zipWith(FStream(1, 2, 3)) { it1, it2 -> it1 + it2 }.take(3).toList())
        Assert.assertEquals(true, stream.startWith(FStream(1, 2, 3)))
        Assert.assertEquals(listOf(10, 9, 7, 4, 0), stream.scanRight(0) { it1, it2 -> it1 + it2 }.toList())

    }

}