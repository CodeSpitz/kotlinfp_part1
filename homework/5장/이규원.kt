package com.kotlin.study.kotlinfp

import com.kotlin.study.kotlinfp.FStream.Cons
import com.kotlin.study.kotlinfp.FStream.Empty

sealed class FStream<out ITEM : Any> {
    object Empty : FStream<Nothing>()
    data class Cons<out ITEM : Any> @PublishedApi internal constructor(
        @PublishedApi internal val head: ITEM,
        @PublishedApi internal val tail: FStream<ITEM>
    ) : FStream<ITEM>()

    companion object {
        inline operator fun <ITEM : Any> invoke(vararg items: ITEM): FStream<ITEM> = items.foldRight(invoke(), ::Cons)
        inline operator fun <ITEM : Any> invoke(): FStream<ITEM> = Empty
    }

    fun <ITEM : Any> empty(): FStream<ITEM> = Empty

    fun <ACC> foldRight(base: () -> ACC, block: (ITEM, () -> ACC) -> ACC): ACC =
        when (this) {
            is Empty -> base()
            is Cons -> block(this.head) { this.tail.foldRight(base, block) }
        }

    fun <ITEM : Any> cons(head: () -> ITEM, tail: () -> FStream<ITEM>): FStream<ITEM> {
        val fhead: ITEM by lazy(head)
        val ftail: FStream<ITEM> by lazy(tail)
        return Cons(fhead, ftail)
    }
}

// 5.1
fun <ITEM : Any> FStream<ITEM>.toList(): FList<ITEM> = when (this) {
    is Empty -> FList.Nil
    is Cons -> FList.Cons(this.head, this.tail.toListUnsafe())
}

// 5.2
fun <ITEM : Any> FStream<ITEM>.take(n: Int): FStream<ITEM> {
    fun go(items: FStream<ITEM>, n: Int): FStream<ITEM> = when (items) {
        is Empty -> empty()
        is Cons ->
            if (n == 0) items
            else go(items.tail, n - 1)
    }
    return go(this, n)
}

// 5.3
fun <ITEM : Any> FStream<ITEM>.taskWhile(block: (ITEM) -> Boolean): FStream<ITEM> =
    when (this) {
        is Empty -> empty()
        is Cons ->
            if (block(this.head)) {
                Cons(this.head, this.tail.taskWhile(block))
            } else {
                empty()
            }
    }

// 5.4
fun <ITEM : Any> FStream<ITEM>.forAll(predicate: (ITEM) -> Boolean): Boolean =
    foldRight({ true }, { a, b -> predicate(a) && b() })

// 5.5
fun <ITEM : Any> FStream<ITEM>.takeWhile(predicate: (ITEM) -> Boolean): FStream<ITEM> =
    foldRight({ empty() }, { head, tail -> if (predicate(head)) cons({ head }, tail) else tail() })

// 5.6
fun <ITEM : Any> FStream<ITEM>.headOption(): Option<ITEM> =
    this.foldRight(
        { Option<ITEM>() },
        { a, _ -> Option(a) }
    )

// 5.7
fun <ITEM : Any, ACC : Any> FStream<ITEM>.map(block: (ITEM) -> ACC): FStream<ACC> =
    this.foldRight(
        { empty<ACC>() },
        { head, tail ->
            cons({ block(head) }, tail)
        })

