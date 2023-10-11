package com.sfn.cancun.controller.admin.support

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class `3강 연습문제` {
    sealed class List<out A> { // List 데이터 구조를 정의함
        companion object { // 함수가 포함된 동반 객체
            fun <A> of(vararg aa: A): List<A> { // 팩토리 도우미 함수
                val tail = aa.sliceArray(1 until aa.size)
                return if (aa.isEmpty()) Nil else Cons(aa[0], of(*tail))
            }

            fun <A> empty(): List<A> = Nil

        }
    }

    object Nil : List<Nothing>() // List의 Nil(빈 리스트) 구현
    data class Cons<out A>(val head: A, val tail: List<A>) : List<A>() // Cons도 List를 구현함


    @DisplayName("연습문제 3.1")
    fun <A> tail(xz: List<A>): List<A> =
        when (xz) {
            is Nil -> Nil
            is Cons -> xz.tail
        }

    @DisplayName("연습문제 3.2")
    fun <A> setHead(xz: List<A>, x: A): List<A> =
        when (xz) {
            is Nil -> Nil
            is Cons -> Cons(x, xz.tail)
        }

    @DisplayName("연습문제 3.3")
    tailrec fun <A> drop(l: List<A>, n: Int): List<A> =
        if (n == 0) l
        else
            when (l) {
                is Nil -> Nil
                is Cons -> drop(l.tail, n - 1)
            }

    @DisplayName("연습문제 3.4")
    tailrec fun <A> dropWhile(l: List<A>, f: (A) -> Boolean): List<A> =
        when (l) {
            is Nil -> Nil
            is Cons ->
                if (f(l.head)) dropWhile(l.tail, f)
                else l
        }

    @DisplayName("연습문제 3.5")
    fun <A> init(l: List<A>): List<A> =
        when (l) {
            is Nil -> Nil
            is Cons ->
                if (l.tail == Nil) Nil
                else
                    Cons(l.head, init(l.tail))
        }

    @DisplayName("연습문제 3.7")
    fun <A, B> foldRight(xs: List<A>, z: B, f: (A, B) -> B): B =
        when (xs) {
            is Nil -> z
            is Cons -> f(xs.head, foldRight(xs.tail, z, f))
        }

    fun sum2(ints: List<Int>): Int =
        foldRight(ints, 0, { a, b -> a + b })

    fun product2(dbs: List<Double>): Double =
        foldRight(dbs, 1.0, { a, b -> a * b })

    @Test
    fun aa() {
//        println(sum2(List.of(1, 2, 3, 4)))
        println(foldRight(
            Cons(1, Cons(2, Cons(3, Nil))),
            Nil as List<Int>,
            { x, y -> Cons(x, y) }
        ))
    }

    @DisplayName("연습문제 3.8")
    fun <A> length(xs: List<A>): Int =
        foldRight(xs, 0, { _, acc -> 1 + acc })

    @DisplayName("연습문제 3.9")
    tailrec fun <A, B> foldLeft(xs: List<A>, z: B, f: (B, A) -> B): B =
        when (xs) {
            is Nil -> z
            is Cons -> foldLeft(xs.tail, f(z, xs.head), f)
        }

    @DisplayName("연습문제 3.10")
    fun sumL(xs: List<Int>): Int =
        foldLeft(xs, 0) { x, y -> x + y }

    fun productL(xs: List<Double>): Double =
        foldLeft(xs, 1.0) { x, y -> x * y }

    fun <A> lengthL(xs: List<A>): Int =
        foldLeft(xs, 0) { acc, _ -> acc + 1 }

    @DisplayName("연습문제 3.11")
    fun <A> reverse(xs: List<A>): List<A> =
        foldLeft(xs, List.empty()) { t: List<A>, h: A -> Cons(h, t) }

    @DisplayName("연습문제 3.12")
    fun <A, B> foldLeftR(xs: List<A>, z: B, f: (B, A) -> B): B =
        foldRight(xs, { b: B -> b }, { a, g -> { b -> g(f(b, a)) } })(z)

    fun <A, B> foldRightL(xs: List<A>, z: B, f: (A, B) -> B): B =
        foldLeftR(xs, { b: B -> b }, { g, a -> { b -> g(f(a, b)) } })(z)

    @DisplayName("연습문제 3.13")
    fun <A> append(a1: List<A>, a2: List<A>): List<A> =
        foldRight(a1, a2) { x, y -> Cons(x, y) }

//    fun <A> appendL(a1: List<A>, a2: List<A>): List<A> =
//        SOLUTION_HERE()

    @DisplayName("연습문제 3.14")
    fun <A> concat(lla: List<List<A>>): List<A> =
        foldRight(lla, List.empty()) { xs1: List<A>, xs2: List<A> -> foldRight(xs1, xs2) { a, ls -> Cons(a, ls) } }

    fun <A> concat2(lla: List<List<A>>): List<A> =
        foldRight(lla, List.empty()) { xs1: List<A>, xs2: List<A> -> append(xs1, xs2) }

    @DisplayName("연습문제 3.15")
    fun increment(xs: List<Int>): List<Int> =
        foldRight(xs, List.empty()) { i, ls -> Cons(i + 1, ls) }

    @DisplayName("연습문제 3.16")
    fun doubleToString(xs: List<Double>): List<String> =
        foldRight(xs, List.empty()) { d, ds -> Cons(d.toString(), ds) }

    @DisplayName("연습문제 3.17")
    fun <A, B> map(xs: List<A>, f: (A) -> B): List<B> =
        foldRight(xs, List.empty()) { a, xa -> Cons(f(a), xa) }

    @DisplayName("연습문제 3.18")
    fun <A> filter(xs: List<A>, f: (A) -> Boolean): List<A> =
        foldRight(xs, List.empty()) { a, ls -> if (f(a)) Cons(a, ls) else ls }

    @DisplayName("연습문제 3.19")
    fun <A, B> flatMap(xa: List<A>, f: (A) -> List<B>): List<B> =
        foldRight(xa, List.empty()) { a, lb -> append(f(a), lb) }

    @DisplayName("연습문제 3.20")
    fun <A> filter2(xa: List<A>, f: (A) -> Boolean): List<A> =
        flatMap(xa) { a -> if (f(a)) List.of(a) else List.empty() }

    @DisplayName("연습문제 3.21")
    fun add(xa: List<Int>, xb: List<Int>): List<Int> =
        when (xa) {
            is Nil -> Nil
            is Cons -> when (xb) {
                is Nil -> Nil
                is Cons -> Cons(xa.head + xb.head, add(xa.tail, xb.tail))
            }
        }

    @DisplayName("연습문제 3.22")
    fun <A> zipWith(xa: List<A>, xb: List<A>, f: (A, A) -> A): List<A> =
        when (xa) {
            is Nil -> Nil
            is Cons -> when (xb) {
                is Nil -> Nil
                is Cons -> Cons(f(xa.head, xb.head), zipWith(xa.tail, xb.tail, f))
            }
        }

    @DisplayName("연습문제 3.23")
    tailrec fun <A> startsWith(l1: List<A>, l2: List<A>): Boolean =
        when (l1) {
            is Nil -> l2 == Nil
            is Cons -> when (l2) {
                is Nil -> true
                is Cons ->
                    if (l1.head == l2.head)
                        startsWith(l1.tail, l2.tail)
                    else false
            }
        }

    tailrec fun <A> hasSubsequence(xs: List<A>, sub: List<A>): Boolean =
        when (xs) {
            is Nil -> false
            is Cons -> {
                if (startsWith(xs, sub)) true
                else hasSubsequence(xs.tail, sub)
            }
        }

    sealed class Tree<out A>
    data class Leaf<A>(val value: A) : Tree<A>()
    data class Branch<A>(val left: Tree<A>, val right: Tree<A>) : Tree<A>()

    @DisplayName("연습문제 3.24")
    fun <A> size(tree: Tree<A>): Int =
        when (tree) {
            is Leaf -> 1
            is Branch -> 1 + size(tree.left) + size(tree.right)
        }

    @DisplayName("연습문제 3.25")
    fun maximum(tree: Tree<Int>): Int =
        when (tree) {
            is Leaf -> tree.value
            is Branch -> maxOf(maximum(tree.left), maximum(tree.right))
        }

    @DisplayName("연습문제 3.26")
    fun depth(tree: Tree<Int>): Int =
        when (tree) {
            is Leaf -> 0
            is Branch -> maxOf(1 + depth(tree.left), 1 + depth(tree.right))
        }

    @DisplayName("연습문제 3.27")
    fun <A, B> map(tree: Tree<A>, f: (A) -> B): Tree<B> =
        when (tree) {
            is Leaf -> Leaf(f(tree.value))
            is Branch -> Branch(map(tree.left, f), map(tree.right, f))
        }

    @DisplayName("연습문제 3.28")
    fun <A, B> fold(ta: Tree<A>, l: (A) -> B, b: (B, B) -> B): B =
        when (ta) {
            is Leaf -> l(ta.value)
            is Branch -> b(fold(ta.left, l, b), fold(ta.right, l, b))
        }

    fun <A> sizeF(ta: Tree<A>): Int =
        fold(ta, { 1 }, { b1, b2 -> 1 + b1 + b2 })

    fun maximumF(ta: Tree<Int>): Int =
        fold(ta, { a -> a }, { b1, b2 -> maxOf(b1, b2) })

    fun <A> depthF(ta: Tree<A>): Int =
        fold(ta, { 0 }, { b1, b2 -> 1 + maxOf(b1, b2) })

    fun <A, B> mapF(ta: Tree<A>, f: (A) -> B): Tree<B> =
        fold(ta, { a: A -> Leaf(f(a)) }, { b1: Tree<B>, b2: Tree<B> -> Branch(b1, b2) })
}
