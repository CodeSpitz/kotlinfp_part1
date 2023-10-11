package com.example.fp_practice_with_kotlin.chapter3

import org.junit.Assert
import org.junit.Test

sealed class List<out A> {
    companion object {
        fun <A> of(vararg xs: A): List<A> {
            val tail = xs.sliceArray(1 until xs.size)
            return if (xs.isEmpty()) Nil else Cons(xs[0], of(*tail))
        }
    }
}

object Nil: List<Nothing>()

data class Cons<out A>(val head: A, val tail: List<A>): List<A>()

fun sum(ints: List<Int>): Int = when(ints) {
    is Nil -> 0
    is Cons -> ints.head + sum(ints.tail)
}

fun product(doubles: List<Double>): Double = when(doubles) {
    is Nil -> 1.0
    is Cons -> if (doubles.head == 0.0) 0.0 else doubles.head * product(doubles.tail)
}

// ex 3.1
// List 의 첫 번째 원소 제거
fun <A> tail(xs: List<A>): List<A> = when(xs) {
    is Cons -> xs.tail
    Nil -> Nil
}

// ex 3.2
// List 의 첫 번째 원소를 다른 값으로 대체
fun <A> setHead(xs: List<A>, x: A): List<A> = Cons(x, tail(xs))

// ex 3.3
// List 의 앞에서부터 n개의 원소 제거
fun <A> drop(l: List<A>, n: Int): List<A> = when(n) {
    0 -> l
    else -> {
        if (l is Cons) drop(tail(l), n - 1)
        else Nil
    }
}

// ex 3.4
// List 의 앞에서부터 주어진 술어를 만족하는 연속적인 원소 제거
fun <A> dropWhile(l: List<A>, f: (A) -> Boolean): List<A> = when(l) {
    Nil -> Nil
    is Cons -> when(f(l.head)) {
        true -> dropWhile(l.tail, f)
        false -> l
    }
}

// ex 3.5
// List 의 마지막 원소를 제외한 모든 원소로 이루어진 List
fun <A> init(l: List<A>): List<A> = when(l) {
    Nil -> l
    is Cons -> when(l.tail) {
        Nil -> Nil
        is Cons -> Cons(l.head, init(l.tail))
    }
}


fun <A, B> foldRight(xs: List<A>, z: B, f: (A, B) -> B): B = when(xs) {
    is Nil -> z
    is Cons -> f(xs.head, foldRight(xs.tail, z, f))
}

fun sum2(ints: List<Int>): Int = foldRight(ints, 0) { a, b -> a + b }

fun product2(dbs: List<Double>): Double = foldRight(dbs, 1.0) { a, b -> a * b }

// ex 3.8
// foldRight를 사용해 리스트 길이 계산
fun <A> length(xs: List<A>): Int = foldRight(xs, 0) { _, b -> b + 1 }

// ex 3.9
// foldRight 를 꼬리재귀로 작성
fun <A, B> foldLeft(xs: List<A>, z: B, f: (B, A) -> B): B = when(xs) {
    is Nil -> z
    is Cons -> foldLeft(xs.tail, f(z, xs.head), f)
}

// ex 3.10
// foldLeft를 사용해 sum, product, 리스트 길이 계산 함수 구현
fun sumFoldLeft(ints: List<Int>): Int = foldLeft(ints, 0) { b, a -> b + a }
fun productFoldLeft(dbs: List<Double>): Double = foldLeft(dbs, 1.0) { b, a -> b * a }
fun <A> lengthFoldLeft(xs: List<A>): Int = foldLeft(xs, 0) { b, _ -> b + 1 }

// ex 3.11
// 접기 연산(foldRight, foldLeft)을 사용해 리스트 뒤집기
fun <A> reverse(xs: List<A>): List<A> = foldLeft(xs, Nil as List<A>) { b, a -> Cons(a, b) }

// ex 3.12
// foldLeft를 foldRight를 이용해 구현
fun <A, B> foldLeft2(xs: List<A>, z: B, f: (B, A) -> B): B = foldRight(reverse(xs), z) { a, b -> f(b, a) }

// ex 3.13
// append를 foldRight나 foldLeft로 구현
fun <A> appendFoldRight(a1: List<A>, a2: List<A>): List<A> = foldRight(a1, a2) { a, b -> Cons(a, b) }
fun <A> appendFoldLeft(a1: List<A>, a2: List<A>): List<A> = foldLeft(reverse(a1), a2) { b, a -> Cons(a, b) }


// ex 3.14
// 리스트가 원소인 리스트를 단일 리스트로 연결
fun <A> concat(xs: List<List<A>>): List<A> = foldRight(xs, Nil as List<A>) { a, b -> appendFoldRight(a, b) }
class Chapter3_14 {
    @Test
    fun main() {
        val lists: List<List<Int>> = List.of(
            List.of(1, 2, 3),
            List.of(4, 5, 6),
            List.of(7, 8, 9)
        )
        Assert.assertEquals(
            List.of(1, 2, 3, 4, 5, 6, 7, 8, 9),
            concat(lists)
        )
    }
}


// ex 3.15
// 정수로 이뤄진 리스트의 각 원소에 1을 더한 리스트 반환
fun addOne(xs: List<Int>): List<Int> = foldRight(xs, Nil as List<Int>) { a, b -> Cons(a + 1, b) }
class Chapter3_15 {
    @Test
    fun main() {
        val list: List<Int> = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9)
        Assert.assertEquals(
            List.of(2, 3, 4, 5, 6, 7, 8, 9, 10),
            addOne(list)
        )
    }
}


// ex 3.16
// List<Double>의 각 값을 String으로 변환
fun doubleToString(xs: List<Double>): List<String> = foldRight(xs, Nil as List<String>) { a, b -> Cons(a.toString(), b) }
class Chapter3_16 {
    @Test
    fun main() {
        val list: List<Double> = List.of(1.0, 2.0, 3.0, 4.0)
        Assert.assertEquals(
            List.of("1.0", "2.0", "3.0", "4.0"),
            doubleToString(list)
        )
    }
}


// ex 3.17
// 리스트의 모든 원소를 변경하되 리스트 구조는 그대로 유지하는 map 함수
fun <A, B> map(xs: List<A>, f: (A) -> B): List<B> = foldRight(xs, Nil as List<B>) { a, b -> Cons(f(a), b) }
class Chapter3_17 {
    @Test
    fun main() {
        val list: List<Int> = List.of(1, 2, 3, 4)
        Assert.assertEquals(
            List.of(2, 3, 4, 5),
            map(list) { it + 1 }
        )
    }
}

// ex 3.18
// 리스트에서 주어진 술어를 만족하지 않는 원소를 제거해주는 filter 함수
fun <A> filter(xs: List<A>, f: (A) -> Boolean): List<A> = foldRight(xs, Nil as List<A>) { a, b -> if (f(a)) Cons(a, b) else b }
class Chapter3_18 {
    @Test
    fun main() {
        val list: List<Int> = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9)
        Assert.assertEquals(
            List.of(3, 6, 9),
            filter(list) { it % 3 == 0 }
        )
    }
}

// ex 3.19
// 인자로 리스트를 반환하는 함수를 받는 flatMap 함수
fun <A, B> flatMap(xs: List<A>, f: (A) -> List<B>): List<B> = concat(map(xs, f))
class Chapter3_19 {
    @Test
    fun main() {
        val list: List<Int> = List.of(1, 2, 3)
        Assert.assertEquals(
            List.of(1, 1, 2, 2, 3, 3),
            flatMap(list) { List.of(it, it) }
        )
    }
}

// ex 3.20
// flatMap을 이용해 filter 구현
fun <A> filterFlatMap(xs: List<A>, f: (A) -> Boolean): List<A> = flatMap(xs) { if (f(it)) List.of(it) else Nil }
class Chapter3_20 {
    @Test
    fun main() {
        val list: List<Int> = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9)
        Assert.assertEquals(
            List.of(3, 6, 9),
            filterFlatMap(list) { it % 3 == 0 }
        )
    }
}

// ex 3.21
// 두 리스트를 받아서 서로 같은 위치에 있는 원소들을 더한 값으로 이뤄진 새 리스트 반환하는 함수
fun addSameIndex(a1: List<Int>, a2: List<Int>): List<Int> = when (a1) {
    Nil -> Nil
    is Cons -> when (a2) {
        Nil -> Nil
        is Cons -> Cons(a1.head + a2.head, addSameIndex(a1.tail, a2.tail))
    }
}
class Chapter3_21 {
    @Test
    fun main() {
        val list1: List<Int> = List.of(1, 2, 3, 5)
        val list2: List<Int> = List.of(4, 5, 6)
        Assert.assertEquals(
            List.of(5, 7, 9),
            addSameIndex(list1, list2)
        )
    }
}

// ex 3.22
// 두 리스트를 받아서 같은 위치에 있는 원소끼리 더한 값으로 이뤄진 새 리스트 반환하는 함수(일반화)
fun <A> zipWith(a1: List<A>, a2: List<A>, f: (A, A) -> A): List<A> = when (a1) {
    Nil -> Nil
    is Cons -> when (a2) {
        Nil -> Nil
        is Cons -> Cons(f(a1.head, a2.head), zipWith(a1.tail, a2.tail, f))
    }
}
class Chapter3_22 {
    @Test
    fun main() {
        val list1: List<Double> = List.of(1.0, 2.0, 3.0, 5.0)
        val list2: List<Double> = List.of(4.0, 5.0, 6.0)
        Assert.assertEquals(
            List.of(5.0, 7.0, 9.0),
            zipWith(list1, list2) { a, b -> a + b }
        )
    }
}

// ex 3.23
// 어떤 List가 다른 List를 부분열로 포함하는지 검사하는 hasSubsequence 함수
tailrec fun <A> hasSubsequence(a1: List<A>, a2: List<A>): Boolean = when (a1) {
    Nil -> false
    is Cons -> when (a2) {
        Nil -> false
        is Cons ->
            if (a1.head == a2.head) {
                if (a2.tail == Nil) true
                else hasSubsequence(a1.tail, a2.tail)
            }
            else hasSubsequence(a1.tail, a2)
    }
}
class Chapter3_23 {
    @Test
    fun main() {
        val list: List<Int> = List.of(1, 2, 3, 4)
        Assert.assertTrue(hasSubsequence(list, List.of(1, 2)))
        Assert.assertTrue(hasSubsequence(list, List.of(2, 3)))
        Assert.assertTrue(hasSubsequence(list, List.of(4)))
    }
}

sealed class Tree<out A>

data class Leaf<A>(val value: A) : Tree<A>()

data class Branch<A>(val left: Tree<A>, val right: Tree<A>) : Tree<A>()

// ex 3.24
// 트리 안에 들어 있는 노드의 개수 반환
fun <A> size(t: Tree<A>): Int = when (t) {
    is Leaf -> 1
    is Branch -> size(t.left) + size(t.right)
}

// ex 3.25
// Tree<Int>에서 가장 큰 원소를 돌려주는 함수
fun maximum(t: Tree<Int>): Int = when (t) {
    is Leaf -> t.value
    is Branch -> maxOf(maximum(t.left), maximum(t.right))
}

// ex 3.26
// 트리 뿌리에서 각 잎까지의 경로 중 가장 길이가 긴 값 반환
fun depth(t: Tree<Int>): Int = when (t) {
    is Leaf -> 1
    is Branch -> maxOf(depth(t.left), depth(t.right)) + 1
}

// ex 3.27
// List에 정의했던 map과 대응하는 map 함수
// 트리의 모든 원소를 주어진 함수를 사용해 변환한 새 함수 반환
fun <A, B> map(t: Tree<A>, f: (A) -> B): Tree<B> = when (t) {
    is Leaf -> Leaf(f(t.value))
    is Branch -> Branch(map(t.left, f), map(t.right, f))
}

// ex 3.28
// Tree에서 size, maximum, depth, map을 일반화
fun <A, B> fold(t: Tree<A>, z: (A) -> B, f: (B, B) -> B): B = when (t) {
    is Leaf -> z(t.value)
    is Branch -> f(fold(t.left, z, f), fold(t.right, z, f))
}

fun <A> sizeF(t: Tree<A>): Int = fold(t, { 1 }, { b1, b2 -> b1 + b2 })

fun maximumF(t: Tree<Int>): Int = fold(t, { it }, { b1, b2 -> maxOf(b1, b2) })

fun <A> depthF(t: Tree<A>): Int = fold(t, { 1 }, { b1, b2 -> maxOf(b1, b2) + 1 })

fun <A, B> mapF(t: Tree<A>, f: (A) -> B): Tree<B> = fold<A, Tree<B>>(t, { Leaf(f(it)) }, { b1, b2 -> Branch(b1, b2) })
class Chapter3_28 {
    @Test
    fun main() {
        val tree1: Tree<Int> = Branch(
            Branch(
                Leaf(1),
                Branch(
                    Leaf(2),
                    Leaf(3)
                )
            ),
            Branch(
                Branch(
                    Leaf(4),
                    Branch(
                        Leaf(5),
                        Branch(
                            Leaf(6),
                            Leaf(7)
                        )
                    )
                ),
                Leaf(8)
            )
        )
        val tree2: Tree<String> = Branch(
            Branch(
                Leaf("1"),
                Branch(
                    Leaf("2"),
                    Leaf("3")
                )
            ),
            Branch(
                Branch(
                    Leaf("4"),
                    Branch(
                        Leaf("5"),
                        Branch(
                            Leaf("6"),
                            Leaf("7")
                        )
                    )
                ),
                Leaf("8")
            )
        )
        Assert.assertEquals(
            size(tree1),
            sizeF(tree1)
        )

        Assert.assertEquals(
            maximum(tree1),
            maximumF(tree1)
        )

        Assert.assertEquals(
            depth(tree1),
            depthF(tree1)
        )

        Assert.assertEquals(
            map(tree1) { it.toString() },
            mapF(tree1) { it.toString() }
        )
    }
}