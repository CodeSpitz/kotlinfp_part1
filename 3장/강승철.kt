package com.example.fp_practice_with_kotlin.chapter3

import org.junit.Assert
import org.junit.Test

sealed class List<out ELEMENT> {
    companion object {
        fun <ELEMENT> of(vararg elements: ELEMENT): List<ELEMENT> {
            return if (elements.isEmpty()) Nil else {
                val tail = elements.sliceArray(1 until elements.size)
                Cons(elements[0], of(*tail))
            }
        }
        fun <ELEMENT> empty(): List<ELEMENT> = Nil
    }
}
object Nil: List<Nothing>()
data class Cons<out ELEMENT>(val head: ELEMENT, val tail: List<ELEMENT>): List<ELEMENT>()
fun <ELEMENT> empty(): List<ELEMENT> = Nil

fun sum(ints: List<Int>): Int {
    tailrec fun _sum(ints: List<Int>, acc: Int): Int =
        when (ints) {
            is Nil -> acc
            is Cons -> _sum(ints.tail, acc + ints.head)
        }
    return _sum(ints, 0)
}
fun product(doubles: List<Double>): Double {
    tailrec fun _product(doubles: List<Double>, acc: Double): Double =
        when (doubles) {
            is Nil -> 1.0
            is Cons ->
                if (doubles.head == 0.0) 0.0
                else _product(doubles.tail, acc * doubles.head)
        }
    return _product(doubles, 1.0)
}

val ex1: List<Double> = Nil
val ex2: List<Int> = Cons(1, Nil)
val ex3: List<String> = Cons("a", Cons("b", Nil))

// ===================================
// ✏️[연습문제 3.1]
//fun <ELEMENT> tail(target: List<ELEMENT>): List<ELEMENT> =
//    when (target) {
//        is Nil -> Nil
//        is Cons -> target.tail
//    }
val <ELEMENT> List<ELEMENT>.tail: List<ELEMENT> get() =
    when (this) {
        is Nil -> Nil
        is Cons -> this.tail
    }
class Chapter3_1 {
    @Test
    fun main() {
        // case 1. Nil 인 경우
        val empty = Nil
        Assert.assertEquals(Nil, empty.tail)

        // case 2. elements 갯수가 하나인 경우
        val list1 = List.of(1)
        Assert.assertEquals(Nil, list1.tail)

        // case 3. elements 갯수가 2개 이상인 경우
        var tail: List<Int>
        var list: List<Int>
        tail = List.of(2)
        list = Cons(1, tail)
        Assert.assertEquals(tail, list.tail)

        tail = List.of(2, 3, 4, 5, 6)
        list = Cons(1, tail)
        Assert.assertEquals(tail, list.tail)
    }
}

// ===================================
// ✏️[연습문제 3.2]
//fun <ELEMENT> setHead(head: ELEMENT, tail: List<ELEMENT>): List<ELEMENT> =
//    Cons(head, tail)
fun <ELEMENT> List<ELEMENT>.addHead(head: ELEMENT): List<ELEMENT> =
    Cons(head, this)
class Chapter3_2 {
    @Test
    fun main() {
        val head: Int = 1
        val list: List<Int> = List.of(2, 3, 4, 5, 6)
        Assert.assertEquals(List.of(1, 2, 3, 4, 5, 6), list.addHead(head))
    }
}

// ===================================
// ✏️[연습문제 3.3]
//tailrec fun <ELEMENT> drop(list: List<ELEMENT>, n: Int): List<ELEMENT> =
//    if (n <= 0) list
//    else when (list) {
//        is Nil -> Nil
//        is Cons -> drop(list.tail, n - 1)
//    }
tailrec fun <ELEMENT> List<ELEMENT>.drop(n: Int): List<ELEMENT> =
    if (n <= 0) this
    else when (this) {
        is Nil -> Nil
        is Cons -> this.tail.drop(n - 1)
    }
class Chapter3_3 {
    @Test
    fun main() {
        val list: List<Int> = List.of(1, 2, 3, 4, 5, 6)
        Assert.assertEquals(List.of(2, 3, 4, 5, 6), list.drop(1))
        Assert.assertEquals(Nil, Nil.drop(100))
    }
}

// ===================================
// ✏️[연습문제 3.4]
tailrec fun <ELEMENT> List<ELEMENT>.drop(predicate: (ELEMENT) -> Boolean): List<ELEMENT> =
    when (this) {
        is Nil -> this
        is Cons -> if (predicate(head)) tail.drop(predicate) else this
    }
class Chapter3_4 {
    @Test
    fun main() {
        val list: List<Int> = List.of(1, 2, 3, 4, 5, 6)
        Assert.assertEquals(
            List.of(4, 5, 6),
            list.drop {
                when (it) {
                    in 1..3 -> true
                    else -> false
                }
            }
        )
    }
}

//fun <ELEMENT> List<ELEMENT>.append(contentsOf: List<ELEMENT>): List<ELEMENT> =
//    when (this) {
//        is Nil -> contentsOf
//        is Cons -> Cons(head, tail.append(contentsOf = contentsOf))
//    }

// ===================================
// ✏️[연습문제 3.5]
fun <ELEMENT> List<ELEMENT>.init(): List<ELEMENT> =
    when (this) {
        is Nil -> this
        is Cons -> if (tail == Nil) Nil else Cons(head, tail.init())
    }
class Chapter3_5 {
    @Test
    fun main() {
        val list: List<Int> = List.of(1, 2, 3, 4, 5, 6)
        Assert.assertEquals(
            List.of(1, 2, 3, 4, 5),
            list.init()
        )
    }
}


// list3.11
/**
 * @param target: target
 * @param initialResult: accumulator
 * @param nextPartialResult: predicate
 */
fun <ELEMENT, ACCUMULATOR> foldRight(
    target: List<ELEMENT>,
    initialResult: ACCUMULATOR,
    nextPartialResult: (ELEMENT, ACCUMULATOR) -> ACCUMULATOR
): ACCUMULATOR =
    when (target) {
        is Nil -> initialResult
        is Cons -> nextPartialResult(target.head, foldRight(target.tail, initialResult, nextPartialResult))
    }
fun <ELEMENT, ACCUMULATOR> List<ELEMENT>.reduceRight(
    initialResult: ACCUMULATOR,
    nextPartialResult: (ELEMENT, ACCUMULATOR) -> ACCUMULATOR
): ACCUMULATOR =
    when (this) {
        is Nil -> initialResult
        is Cons -> nextPartialResult(head, tail.reduceRight(initialResult, nextPartialResult))
    }
fun sum2(ints: List<Int>): Int = foldRight(ints, 0) { a, b -> a + b }
fun product2(dbs: List<Double>): Double = foldRight(dbs, 0.0) { a, b -> a * b }
class Chapter3_list_11 {
    @Test
    fun main() {
        val list: List<Int> = List.of(1, 2, 3, 4, 5, 6)
        Assert.assertEquals(
            (1 + 6) * 6 / 2,
            foldRight(
                List.of(1, 2, 3, 4, 5, 6),
                0
            ) { curr, accu -> accu + curr }
        )
        Assert.assertEquals(
            (1 + 6) * 6 / 2,
            List.of(1, 2, 3, 4, 5, 6)
                .reduceRight(0) {  curr, accu -> accu + curr }
        )
    }
}

// ===================================
// ✏️[연습문제 3.6]
// 지금 구조로는 방법이 없음.
// 모든 아이템을 다 순회하지 않아도 되므로 퍼포먼스에 장점이 있을 것으로 판단

// ===================================
// ✏️[연습문제 3.7]
class Chapter3_7 {
    @Test
    fun main() {
        val folded: List<Int> = foldRight(
            List.of(1, 2, 3, 4),
            // 최종 결과가 accumulator 타입으로 결정됨.
            // 그러나 accumulator의 초기값인 List.empty가 고정타입이 아니므로
            // 수신부에서 List<Int>를 넣어 타입추정의 힌트를 전달해야할 필요가 있음
            // 👇👇👇
            List.empty()
        ) { curr, accu ->
            Cons(curr, accu)
        }
        println(folded)
    }
}

// ===================================
// ✏️[연습문제 3.8]
//fun <ELEMENT> length(xs: List<ELEMENT>): Int =
//    foldRight(xs, 0) { _, accu -> accu + 1 }
fun <ELEMENT> List<ELEMENT>.length(): Int =
    reduceRight(0) { _, accu -> accu + 1 }
class Chapter3_8 {
    @Test
    fun main() {
        val list: List<Int> = List.of(1, 2, 3, 4, 5, 6)
        Assert.assertEquals(
            6,
            list.length()
        )
    }
}

// ===================================
// ✏️[연습문제 3.9]
fun <ELEMENT, ACCUMULATOR> foldLeft(
    target: List<ELEMENT>,
    initialResult: ACCUMULATOR,
    nextPartialResult: (ACCUMULATOR, ELEMENT) -> ACCUMULATOR
): ACCUMULATOR =
    when (target) {
        is Nil -> initialResult
        is Cons -> foldLeft(target.tail, nextPartialResult(initialResult, target.head), nextPartialResult)
    }
fun <ELEMENT, ACCUMULATOR> List<ELEMENT>.reduce(
    initialResult: ACCUMULATOR,
    nextPartialResult: (ACCUMULATOR, ELEMENT) -> ACCUMULATOR
): ACCUMULATOR =
    when (this) {
        is Nil -> initialResult
        is Cons ->
            tail.reduce(
                nextPartialResult(initialResult, head), // 이게 다음 accu
                nextPartialResult
            )
    }
class Chapter3_9 {
    @Test
    fun main() {
        val list: List<Int> = List.of(1, 2, 3, 4, 5, 6)
        Assert.assertEquals(
            list.reduceRight(0) { curr, accu -> accu + curr },
            list.reduce(0) {  accu, curr -> accu + curr }
        )
    }
}

// ===================================
// ✏️[연습문제 3.10]
fun List<Int>.sumWithReduce(): Int =
    reduce(0) { accu, curr -> accu + curr }
fun List<Double>.productWithReduce(): Double =
    reduce(1.0) { accu, curr -> accu * curr }

// ===================================
// ✏️[연습문제 3.11]
fun <ELEMENT> List<ELEMENT>.reverse(): List<ELEMENT> =
    reduce(List.empty()) { accu, curr -> Cons(curr, accu) }
class Chapter3_11 {
    @Test
    fun main() {
        val list: List<Int> = List.of(1, 2, 3, 4, 5, 6)
        Assert.assertEquals(
            List.of(6, 5, 4, 3, 2, 1),
            list.reverse()
        )
    }
}

// ===================================
// ✏️[연습문제 3.12]
fun <ELEMENT, ACCUMULATOR> foldRightWithLeft(
    target: List<ELEMENT>,
    initialResult: ACCUMULATOR,
    nextPartialResult: (ELEMENT, ACCUMULATOR) -> ACCUMULATOR
): ACCUMULATOR =
    // (ACCUMULATOR) -> ACCUMULATOR 을 foldLeft의 initialResult의 return type으로 만드는 것이 핵심
    // 이전 증분 결과값이 입력되면 다음 증분 결과값를 생산할 수 있는 클로져를 nextPartialResult로 설정해야 됨.
    foldLeft(
        target,
        { accu: ACCUMULATOR -> accu },
        { accu: (ACCUMULATOR) -> ACCUMULATOR, curr: ELEMENT ->
            { accu(nextPartialResult(curr, it)) }
        }
    )(initialResult)

fun <ELEMENT, ACCUMULATOR> List<ELEMENT>.reduceWithRight(
    initialResult: ACCUMULATOR,
    nextPartialResult: (ELEMENT, ACCUMULATOR) -> ACCUMULATOR
): ACCUMULATOR =
    reduceRight(
        { accu: ACCUMULATOR -> accu },
        { curr: ELEMENT, accu: (ACCUMULATOR) -> ACCUMULATOR ->
            { accu(nextPartialResult(curr, it)) }
        }
    )(initialResult)

fun <ELEMENT, ACCUMULATOR> foldLeftWithRight(
    target: List<ELEMENT>,
    initialResult: ACCUMULATOR,
    nextPartialResult: (ACCUMULATOR, ELEMENT) -> ACCUMULATOR
): ACCUMULATOR =
    foldRight(
        target,
        { accu: ACCUMULATOR -> accu },
        { curr: ELEMENT, accu: (ACCUMULATOR) -> ACCUMULATOR ->
            { accu(nextPartialResult(it, curr)) }
        }
    )(initialResult)

fun <ELEMENT, ACCUMULATOR> List<ELEMENT>.reduceWithLeft(
    initialResult: ACCUMULATOR,
    nextPartialResult: (ACCUMULATOR, ELEMENT) -> ACCUMULATOR
): ACCUMULATOR =
    reduce(
        { accu: ACCUMULATOR -> accu },
        { accu: (ACCUMULATOR) -> ACCUMULATOR, curr: ELEMENT ->
            { accu(nextPartialResult(it, curr)) }
        }
    )(initialResult)

// ===================================
// ✏️[연습문제 3.13]
// 대상이 예를 들어 List.of(1, 2, 3, 4, 5) 이라면 5뒤에 붙어야 하므로 right folding이용해 역순으로 Cons로 감아야 한다
fun <ELEMENT> List<ELEMENT>.append(contentsOf: List<ELEMENT>): List<ELEMENT> =
    reduceRight(
        initialResult = contentsOf,
        nextPartialResult = { curr, accu -> Cons(curr, accu) }
    )
class Chapter3_13 {
    @Test
    fun main() {
        val list1: List<Int> = List.of(1, 2, 3, 4, 5)
        val list2: List<Int> = List.of(6, 7, 8)
        Assert.assertEquals(
            List.of(1, 2, 3, 4, 5, 6, 7, 8),
            list1.append(contentsOf = list2)
        )
    }
}

// ===================================
// ✏️[연습문제 3.14]
// flatten을 구현한다.
fun <ELEMENT> List<List<ELEMENT>>.flatten(): List<ELEMENT> =
    reduceRight(List.empty()) { curr, accu ->
        curr.reduceRight(accu) { curr, accu -> Cons(curr, accu) }
    }
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
            lists.flatten()
        )
    }
}

// ===================================
// ✏️[연습문제 3.15]
fun List<Int>.increase(num: Int): List<Int> =
    reduceRight(List.empty()) { curr, accu -> Cons(curr + num, accu) }
class Chapter3_15 {
    @Test
    fun main() {
        val list: List<Int> = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9)
        Assert.assertEquals(
            List.of(2, 3, 4, 5, 6, 7, 8, 9, 10),
            list.increase(1)
        )
    }
}

// ===================================
// ✏️[연습문제 3.16]
fun List<Double>.toStringList(): List<String> =
    reduceRight(List.empty()) { curr, accu -> Cons(curr.toString(), accu) }
class Chapter3_16 {
    @Test
    fun main() {
        val list: List<Double> = List.of(1.0, 2.0, 3.0, 4.0)
        Assert.assertEquals(
            List.of("1.0", "2.0", "3.0", "4.0"),
            list.toStringList()
        )
    }
}

// ===================================
// ✏️[연습문제 3.17]
fun <ELEMENT, TO> List<ELEMENT>.map(transform: (ELEMENT) -> TO): List<TO> =
    reduceRight(List.empty()) { curr, accu -> Cons(transform(curr), accu) }
class Chapter3_17 {
    @Test
    fun main() {
        val list: List<Double> = List.of(1.0, 2.0, 3.0, 4.0)
        Assert.assertEquals(
            List.of("1.0", "2.0", "3.0", "4.0"),
            list.map { it.toString() }
        )
    }
}

// ===================================
// ✏️[연습문제 3.18]
fun <ELEMENT> List<ELEMENT>.filter(predicate: (ELEMENT) -> Boolean): List<ELEMENT> =
    reduceRight(List.empty()) { curr, accu ->
        if (predicate(curr)) Cons(curr, accu) else accu
    }
class Chapter3_18 {
    @Test
    fun main() {
        val list: List<Double> = List.of(1.0, 2.0, 3.0, 4.0)
        Assert.assertEquals(
            List.of(1.0, 2.0, 4.0),
            list.filter { it != 3.0 }
        )
    }
}

// ===================================
// ✏️[연습문제 3.19]
fun <ELEMENT, TO> List<ELEMENT>.flatMap(transform: (ELEMENT) -> List<TO>): List<TO> =
    reduceRight(List.empty()) { curr, accu ->
        transform(curr).reduceRight(accu) { curr, accu -> Cons(curr, accu) }
    }
class Chapter3_19 {
    @Test
    fun main() {
        val list: List<Int> = List.of(1, 2, 3)
        Assert.assertEquals(
            List.of(1, 1, 2, 2, 3, 3),
            list.flatMap { List.of(it, it) }
        )
    }
}

// ===================================
// ✏️[연습문제 3.20]
fun <ELEMENT> List<ELEMENT>.filter2(predicate: (ELEMENT) -> Boolean): List<ELEMENT> =
    flatMap { if (predicate(it)) List.of(it) else List.empty() }
class Chapter3_20 {
    @Test
    fun main() {
        val list: List<Double> = List.of(1.0, 2.0, 3.0, 4.0)
        Assert.assertEquals(
            List.of(1.0, 2.0, 4.0),
            list.filter2 { it != 3.0 }
        )
    }
}

// ===================================
// ✏️[연습문제 3.21~22]
fun <ELEMENT> List<ELEMENT>.add(
    contentsOf: List<ELEMENT>,
    sum: (ELEMENT, ELEMENT) -> ELEMENT
): List<ELEMENT> =
    when (this) {
        is Nil -> Nil
        is Cons -> {
            when (contentsOf) {
                is Nil -> Nil
                is Cons -> Cons(sum(this.head, contentsOf.head), this.tail.add(contentsOf.tail, sum))
            }
        }
    }

fun List<Int>.add(contentsOf: List<Int>): List<Int> =
    add(contentsOf) { n1, n2 -> n1 + n2 }
class Chapter3_21 {
    @Test
    fun main() {
        val list1: List<Int> = List.of(1, 2, 3, 4, 5)
        val list2: List<Int> = List.of(1, 2, 3, 4)
        Assert.assertEquals(
            List.of(2, 4, 6, 8),
            list1.add(list2)
        )
    }
}

class Chapter3_22 {
    @Test
    fun main() {
        val list1: List<Double> = List.of(1.0, 2.0, 3.0, 4.0, 5.0)
        val list2: List<Double> = List.of(1.0, 2.0, 3.0, 4.0)
        Assert.assertEquals(
            List.of(2.0, 4.0, 6.0, 8.0),
            list1.add(list2) { num1, num2 -> num1 + num2 }
        )
    }
}

// ===================================
// ✏️[연습문제 3.23]
fun <ELEMENT> List<ELEMENT>.hasContain(el: ELEMENT): Boolean =
    reduce(false) { accu, curr -> accu || curr == el }
tailrec fun <ELEMENT> List<ELEMENT>.hasSubsequence(contentsOf: List<ELEMENT>): Boolean =
    when (contentsOf) {
        is Nil -> true
        is Cons -> if (hasContain(contentsOf.head)) hasSubsequence(contentsOf.tail) else false
    }
class Chapter3_23 {
    @Test
    fun main() {
        var list1: List<Int> = List.of(1, 2, 3, 4, 5)
        var list2: List<Int> = List.of(1, 2, 3, 4)
        Assert.assertEquals(
            true,
            list1.hasSubsequence(list2)
        )

        list1 = List.of(1, 2, 3, 4, 5)
        list2 = List.of(10, 20)
        Assert.assertEquals(
            false,
            list1.hasSubsequence(list2)
        )
    }
}

sealed class Tree<out VALUE>
data class Leaf<VALUE>(val value: VALUE): Tree<VALUE>()
data class Branch<VALUE>(val left: Tree<VALUE>, val right: Tree<VALUE>): Tree<VALUE>()


// ===================================
// ✏️[연습문제 3.24]
fun <VALUE> Tree<VALUE>.size(): Int =
    when (this) {
        is Leaf -> 1
        is Branch -> left.size() + right.size() + 1
    }
class Chapter3_24 {
    @Test
    fun main() {
        var tree: Tree<Int> = Branch(
            Branch(
                Leaf(1),
                Branch(
                    Leaf(2),
                    Leaf(3)
                )
            ),
            Leaf(4)
        )
        Assert.assertEquals(
            7,
            tree.size()
        )
    }
}

// ===================================
// ✏️[연습문제 3.25]
fun <VALUE> Leaf<VALUE>.maximum(max: (VALUE, VALUE) -> VALUE): VALUE = value
fun <VALUE> Branch<VALUE>.maximum(max: (VALUE, VALUE) -> VALUE): VALUE =
    max(left.maximum(max), right.maximum(max))
fun <VALUE> Tree<VALUE>.maximum(max: (VALUE, VALUE) -> VALUE): VALUE =
    when (this) {
        is Leaf -> maximum(max)
        is Branch -> max(left.maximum(max), right.maximum(max))
    }
class Chapter3_25 {
    @Test
    fun main() {
        var tree: Tree<Int> = Branch(
            Branch(
                Leaf(1),
                Branch(
                    Leaf(2),
                    Leaf(3)
                )
            ),
            Leaf(4)
        )
        Assert.assertEquals(
            4,
            tree.maximum { n1, n2 -> maxOf(n1, n2) }
        )
    }
}

// ===================================
// ✏️[연습문제 3.26]
fun <VALUE> Tree<VALUE>.depth(): Int =
    when (this) {
        is Leaf -> 0
        is Branch -> maxOf(left.depth(), right.depth()) + 1
    }
class Chapter3_26 {
    @Test
    fun main() {
        var tree: Tree<Int> = Branch(
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
        Assert.assertEquals(
            5,
            tree.depth()
        )
    }
}

// ===================================
// ✏️[연습문제 3.27]
fun <VALUE, TO> Tree<VALUE>.map(transform: (VALUE) -> TO): Tree<TO> =
    when (this) {
        is Leaf -> Leaf(transform(value))
        is Branch -> Branch(left.map(transform), right.map(transform))
    }
class Chapter3_27 {
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
            tree2,
            tree1.map { it.toString() }
        )
    }
}

// ===================================
// ✏️[연습문제 3.28]
fun <VALUE, ACCUMULTOR> Tree<VALUE>.fold(
    leafFold: (VALUE) -> ACCUMULTOR,
    branchFold: (ACCUMULTOR, ACCUMULTOR) -> ACCUMULTOR
): ACCUMULTOR =
    when (this) {
        is Leaf -> leafFold(value)
        is Branch -> branchFold(
            left.fold(leafFold, branchFold),
            right.fold(leafFold, branchFold)
        )
    }
fun <VALUE> Tree<VALUE>.sizeF(): Int =
    this.fold(
        leafFold = { 1 },
        branchFold = { n1, n2 -> n1 + n2 + 1 }
    )
fun <VALUE> Tree<VALUE>.maximumF(max: (VALUE, VALUE) -> VALUE): VALUE =
    this.fold(
        leafFold = { it },
        branchFold = { n1, n2 -> max(n1, n2) }
    )
fun <VALUE> Tree<VALUE>.depthF(): Int =
    this.fold(
        leafFold = { 0 },
        branchFold = { n1, n2 -> maxOf(n1, n2) + 1 }
    )
fun <VALUE, TO> Tree<VALUE>.mapF(transform: (VALUE) -> TO): Tree<TO> =
    this.fold(
        leafFold = { Leaf(transform(it)) },
        branchFold = { left: Tree<TO>, right: Tree<TO> -> Branch(left, right) }
    )
//    when (this) {
//        is Leaf -> Leaf(transform(value))
//        is Branch -> Branch(left.map(transform), right.map(transform))
//    }
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
            tree1.size(),
            tree1.sizeF()
        )

        Assert.assertEquals(
            tree1.maximum { n1, n2 -> maxOf(n1, n2) },
            tree1.maximumF { n1, n2 -> maxOf(n1, n2) }
        )

        Assert.assertEquals(
            tree1.depth(),
            tree1.depthF()
        )

        Assert.assertEquals(
            tree1.map { it.toString() },
            tree1.mapF { it.toString() }
        )
    }
}