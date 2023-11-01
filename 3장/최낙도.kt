package study.fp

import study.fp.FList.*
import study.fp.FTree.*

sealed class FList<out A> {
    object Nil: FList<Nothing>()
    data class Cons<out A>(val head: A, val tail: FList<A>): FList<A>()
}
fun <A> of(vararg aa: A): FList<A> {
    val tail = aa.sliceArray(1 until aa.size)
    return if (aa.isEmpty()) Nil else Cons(aa[0], of(*tail))
}
//fun<A> empty(): FList<A> = Nil
fun<A, B> foldRight(xs: FList<A>, z: B, f: (A, B) -> B): B =
    when (xs) {
        is Nil -> z
        is Cons -> f(xs.head, foldRight(xs.tail, z, f))
    }


/* 연습문제 3.1 */
fun <A> tail(xs: FList<A>): FList<A> =
    when (xs) {
        is Nil -> Nil
        is Cons -> xs.tail
    }

/* 연습문제 3.2 */
fun <A> setHead(xs: FList<A>, x: A): FList<A> =
    when (xs) {
        is Nil -> Nil
        is Cons -> Cons(x, xs.tail)
    }

/* 연습문제 3.3 */
fun <A> drop(l: FList<A>, n: Int): FList<A> {
    tailrec fun loop(xs: FList<A>, count: Int): FList<A> =
        if (count == 0) xs
        else when (xs) {
            is Nil -> Nil
            is Cons -> loop(xs.tail, count - 1)
        }
    return if (n > 0) loop(l, n) else l
}

/* 연습문제 3.4 */
fun <A> dropWhile(l: FList<A>, f: (A) -> Boolean): FList<A> {
    tailrec fun loop(xs: FList<A>, f: (A) -> Boolean): FList<A> {
        return when (xs) {
            is Nil -> Nil
            is Cons -> if (xs.head == Nil) Nil else loop(xs.tail, f)
        }
    }
    return loop(l, f)
}

/*
 * 연습문제 3.5
 * 마지막 원소를 삭제하게 된다면 해당 List의 참조가 변경되어 외부에 있는 List가 변경되는 부수 효과가 일어나기 때문이다.
*/
fun <A> init(l: FList<A>): FList<A> =
    when (l) {
        is Nil -> Nil
        is Cons -> Cons(l.head, init(l.tail))
    }

/*
 * 연습문제 3.6
 * 즉시 중단하고 결과를 돌려 줄 수 있다고 생각한다. 함수에 0.0을 만나면 중단하는 식을 추가하면 된다.
 * 쇼킷 서킷을 제공하면 해당 리스트에 대한 순회를 전부 하지 않고 순회를 중단하기 때문에 성능상에 이점과
 * 해당 쇼킷 서킷으로 인한 중단 조건에서 굳이 계산하지 않아도 되는 상황과
 * 예외적인 상황에 대한(곱하기에 0을 곱하다던가) 상황을 추가함으로써 해당 함수에 대한 이해를 증진시킨다고 생각한다.
 */

/*
 * 연습문제 3.7
 * Nil과 Cons를 foldRight에 넘길 때 각각 인자로 들어온 Cons들은 복사되어서 전달된다.
 */

/* 연습문제 3.8 */
fun <A> length(xs: FList<A>): Int =
    foldRight(xs, 0) { _, a -> a + 1 }


/* 연습문제 3.9 */
tailrec fun <A, B> foldLeft(xs: FList<A>, z: B, f: (B, A) -> B): B =
    when (xs) {
        is Nil -> z
        is Cons -> foldLeft(xs.tail, f(z, xs.head), f)
    }

/* 연습문제 3.10 */
fun sumByFoldLeft(ints: FList<Int>): Int =
    foldLeft(ints, 0) { a, b -> a + b }
fun productByFoldLeft(dbs: FList<Double>): Double =
    foldLeft(dbs, 1.0) { a, b -> a * b }
fun <A> lengthByFoldLeft(xs: FList<A>): Int =
    foldLeft(xs, 0) { a, _ -> a + 1 }

/* 연습문제 3.11 */
fun <A> reverse(xs: FList<A>): FList<A> =
    foldLeft(xs, Nil) { a: FList<A>, b -> Cons(b, a) }

/* 연습문제 3.12 */
fun <A, B> foldLeftR(list: FList<A>, z: B, f: (B, A) -> B): B =
    foldRight(list, { b: B -> b }, { a, g -> { b -> g(f(b, a)) } })(z)

/* 연습문제 3.13 */
fun <A> appendByFoldLeft(list1: FList<A>, list2: FList<A>): FList<A> =
    foldLeft(reverse(list1), list2) { a, b -> Cons(b, a) }
fun <A> appendByFoldRight(list1: FList<A>, list2: FList<A>): FList<A> =
    foldRight(list1, list2) { a, b -> Cons(a, b)}

/* 연습문제 3.14 */
fun <A> concat(list: FList<FList<A>>): FList<A> =
    foldRight(list, empty()) { list1: FList<A>, list2: FList<A> ->
        foldRight(list1, list2) { a, b -> Cons(a, b) }
    }

/* 연습문제 3.15 */
fun addOne(list: FList<Int>): FList<Int> =
    foldRight(list, empty()) { a, b ->  Cons(a + 1, b)}

/* 연습문제 3.16 */
fun convertToString(list: FList<Double>): FList<String> =
    foldRight(list, empty()) { a, b -> Cons(a.toString(), b) }

/* 연습문제 3.17 */
fun <P1, RESULT> map(list: FList<P1>, f: (P1) -> RESULT): FList<RESULT> =
    foldRight(list, empty()) { a, b -> Cons(f(a), b) }

/* 연습문제 3.18 */
fun <P1> filter(list: FList<P1>, f: (P1) -> Boolean): FList<P1> =
    foldRight(list, empty()) { a: P1, b: FList<P1> -> if (f(a)) Cons(a, b) else b }

/* 연습문제 3.19 */
fun <P1, RESULT> flatMap(list: FList<P1>, f: (P1) -> FList<RESULT>): FList<RESULT> =
    foldRight(list, empty()) { a: P1, b: FList<RESULT> -> appendByFoldRight(f(a), b) }

/* 연습문제 3.20 */
fun <P1> filterByFlatMap(list: FList<P1>, f: (P1) -> Boolean): FList<P1> =
    flatMap(list) { a: P1 -> if (f(a)) of(a) else empty() }

/* 연습문제 3.21 */
fun add(list1: FList<Int>, list2: FList<Int>): FList<Int> =
    when (list1) {
        is Nil -> Nil
        is Cons -> when (list2) {
            is Nil -> Nil
            is Cons -> Cons(list1.head + list2.head, add(list1.tail, list2.tail))
        }
    }

/* 연습문제 3.22 */
fun <P1> zipWith(list1: FList<P1>, list2: FList<P1>, f: (P1, P1) -> P1): FList<P1> =
    when (list1) {
        is Nil -> Nil
        is Cons -> when (list2) {
            is Nil -> Nil
            is Cons -> Cons(f(list1.head, list1.head), zipWith(list1.tail, list2.tail, f))
        }
    }

/* 연습문제 3.23 */
tailrec fun <P1> startWith(list1: FList<P1>, list2: FList<P1>): Boolean =
    when (list1) {
        is Nil -> list2 == Nil
        is Cons -> when (list2) {
            is Nil -> true
            is Cons -> if (list1.head == list2.head) startWith(list1.tail, list2.tail) else false
        }
    }

tailrec fun <P1> hasSubsequence(list1: FList<P1>, list2: FList<P1>): Boolean =
    when (list1) {
        is Nil -> false
        is Cons -> if (startWith(list1, list2)) true else hasSubsequence(list1.tail, list2)
    }

sealed class FTree<out A> {
    data class Leaf<A>(val value: A): FTree<A>()
    data class Branch<A>(val left: FTree<A>, val right: FTree<A>): FTree<A>()
}


/* 연습문제 3.24 */
fun <A> size(tree: FTree<A>): Int =
    when (tree) {
        is Leaf -> 1
        is Branch -> 1 + size(tree.left) + size(tree.right)
    }

/* 연습문제 3.25 */
fun <A> maximum(tree: FTree<A>): Int {
    tailrec fun loop(tree: FTree<A>, maxValue: Int): Int =
        when (tree) {
            is Leaf -> maxValue
            is Branch -> minOf(loop(tree.left, maxValue), loop(tree.right, maxValue))
        }
    return loop(tree, Int.MIN_VALUE)
}

/* 연습문제 3.26 */
fun depth(tree: FTree<Int>): Int =
    when (tree) {
        is Leaf -> 0
        is Branch -> 1 + maxOf(depth(tree.left), depth(tree.right))
    }

/* 연습문제 3.27 */
fun <A, B> map(tree: FTree<A>, f: (A) -> B): FTree<B> =
    when (tree) {
        is Leaf -> Leaf(f(tree.value))
        is Branch -> Branch(map(tree.left, f), map(tree.right, f))
    }

/* 연습문제 3.28 */
fun <A, B> fold(tree: FTree<A>, f1: (A) -> B, f2: (B, B) -> B): B =
    when (tree) {
        is Leaf -> f1(tree.value)
        is Branch -> f2(fold(tree.left, f1, f2), fold(tree.right, f1, f2))
    }

fun <A> sizeByFold(tree: FTree<A>): Int =
    fold(tree, { 1 }, { a, b -> 1 + a + b })
fun <A> maximumByFold(tree: FTree<A>): Int =
    fold(tree, { Int.MIN_VALUE }, { b: Int, c: Int -> maxOf(b, c) })
fun depthByFold(tree: FTree<Int>): Int =
    fold(tree, { 0 }, { a, b -> 1 + maxOf(a, b) })
fun <A, B> mapByFold(tree: FTree<A>, f: (A) -> B): FTree<B> =
    fold(tree, { a -> Leaf(f(a)) }, { a: FTree<B>, b: FTree<B> -> Branch(a, b) })
