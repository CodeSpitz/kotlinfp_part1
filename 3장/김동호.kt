sealed class List<out A> {
    companion object {
        fun <A> of(vararg aa: A): List<A> {
            val tail = aa.sliceArray(1..< aa.size)
            return if (aa.isEmpty()) Nil else Cons(aa[0], of(*tail))
        }
    }
}

data object Nil : List<Nothing>()

data class Cons<out A>(val head: A, val tail: List<A>): List<A>()

fun <A> empty(): List<A> = Nil

fun sum(ints: List<Int>): Int =
    when (ints) {
        is Nil -> 0
        is Cons -> ints.head + sum(ints.tail)
    }

fun product(doubles: List<Double>): Double =
    when (doubles) {
        is Nil -> 1.0
        is Cons ->
            if (doubles.head == 0.0) 0.0
            else doubles.head * product(doubles.tail)
    }

// 3.1
fun <A> tail(xs: List<A>):List<A> =
    when (xs) {
        is Nil -> Nil
        is Cons -> xs.tail
    }
// 3.2
fun <A> setHead(xs: List<A>, x: A): List<A> =
    when(xs) {
        is Nil -> Cons(x, Nil)
        is Cons -> Cons(x, xs.tail)
    }
// 3.3
fun <A> drop(l: List<A>, n: Int): List<A> {
    tailrec fun loop(cnt: Int, ll: List<A>): List<A> =
        if (cnt == 0) ll
        else when (ll) {
            is Nil -> Nil
            is Cons -> loop(cnt - 1, ll.tail)
        }
    return if (n <= 0) l else loop(n, l)
}
// 3.4
fun <A> dropWhile(l: List<A>, f: (A) -> Boolean): List<A> {
    tailrec fun loop(ll: List<A>): List<A> =
        when(ll) {
            is Nil -> Nil
            is Cons -> if (f(ll.head)) loop(ll.tail) else ll
        }
    return loop(l)
}

fun <A> append(a1: List<A>, a2: List<A>): List<A> =
    when (a1) {
        is Nil -> a2
        is Cons -> Cons(a1.head, append(a1.tail, a2))
    }
// 3.5
fun <A> init(l: List<A>): List<A> =
    when (l) {
        is Nil -> Nil
        is Cons -> when(l.tail) {
            is Nil -> Nil
            is Cons -> Cons(l.head, init(l.tail))
        }
    }

fun <A, B> foldRight(xs: List<A>, z: B, f: (A, B) -> B): B =
    when (xs) {
        is Nil -> z
        is Cons -> f(xs.head, foldRight(xs.tail, z, f))
    }

fun sum2(ints: List<Int>): Int = foldRight(ints, 0) { a, b -> a + b }

fun product2(doubles: List<Double>): Double = foldRight(doubles, 1.0) { a, b -> a * b}

// 3.6
// foldRight의 유일한 종료 조건은 xs가 Nil인 경우이다.
// 리스트 원소로 0.0을 만난 경우에 재귀를 즉시 중단하기 위해서는 종료 조건을 추가할 수 있거나 xs를 Nil로 전달할 수 있는 수단이 외부로 주어져야 한다.
// 둘 다 제공하지 않으므로 별도의 조건에 따라 재귀가 즉시 중단되도록 할 수는 없다.

// 3.8
fun <A> length(xs: List<A>): Int = foldRight(xs, 0) { _, b -> b + 1 }

// 3.9
tailrec fun <A, B> foldLeft(xs: List<A>, z: B, f: (B, A) -> B) : B =
    when (xs) {
        is Nil -> z
        is Cons -> foldLeft(xs.tail, f(z, xs.head), f)
    }

// 3.10
fun sum3(l: List<Int>) = foldLeft(l, 0) { b , a -> b + a}

fun product3(l: List<Double>) = foldLeft(l, 1.0) { b, a -> b * a}

fun <A> length2(l: List<A>) = foldLeft(l, 0) { a, _ -> a + 1 }

// 3.11
fun <A> reverse(l: List<A>) = foldLeft(l, empty<A>()) { b, a -> Cons(a, b)}

fun <A> reverse2(l: List<A>) = foldRight(l, empty<A>()) { a, b->  foldRight(b, Cons(a, Nil)) { bb, aa -> Cons(bb, aa) } }

// 3.12
fun <A, B> foldLeft2(xs: List<A>, z: B, f: (B, A) -> B): B = foldRight(reverse2(xs), z) { a, b-> f(b, a) }

fun <A, B> foldRight2(xs: List<A>, z: B, f: (A, B) -> B): B = foldLeft(reverse(xs), z) { b, a -> f(a, b)}

// 3.13
fun <A> append2(a1: List<A>, a2: List<A>) = foldRight2(a1, a2) { a, b -> Cons(a, b) }

// 3.14
fun <A> flatten(xs: List<List<A>>) = foldRight2(xs, empty<A>()) { a, b -> append2(a, b)}

// 3.15
fun increment(xs: List<Int>) = foldRight2(xs, empty<Int>()) { a, b -> Cons(a + 1, b) }

// 3.16
fun toString(xs: List<Double>) = foldRight2(xs, empty<String>()) { a, b -> Cons(a.toString(), b) }

// 3.17
fun <A, B> map(xs: List<A>, f: (A) -> B): List<B> = foldRight2(xs, empty<B>()) { a, b -> Cons(f(a), b) }

// 3.18
fun <A> filter(xs: List<A>, f: (A) -> Boolean): List<A> = foldRight2(xs, empty<A>()) { a, b -> if(f(a)) Cons(a, b) else b }

// 3.19
fun <A, B> flatMap(xa: List<A>, f: (A) -> List<B>): List<B> = flatten(map(xa, f))

// 3.20
fun <A> filter2(xs: List<A>, f: (A) -> Boolean): List<A> = flatMap(xs) { if(f(it)) List.of(it) else empty() }

// 3.21
fun ListAdd(a1: List<Int>, a2: List<Int>): List<Int> =
    when (a1) {
        is Nil -> Nil
        is Cons -> when (a2) {
            is Nil -> Nil
            is Cons -> Cons(a1.head + a2.head, ListAdd(a1.tail, a2.tail))
        }
    }
// 3.22
fun <A> zipWith(a1: List<A>, a2: List<A>, f: (A, A) -> A): List<A> =
    when (a1) {
        is Nil -> Nil
        is Cons -> when (a2) {
            is Nil -> Nil
            is Cons -> Cons(f(a1.head, a2.head), zipWith(a1.tail, a2.tail, f))
        }
    }
// 3.23
tailrec fun <A> startWith(l1: List<A>, l2: List<A>): Boolean =
    when (l1) {
        is Nil -> l2 == Nil
        is Cons -> when (l2) {
            is Nil -> true
            is Cons ->
                if (l1.head == l2.head) startWith(l1.tail, l2.tail)
                else false
        }
    }

tailrec fun <A> hasSubsequence(xs: List<A>, sub: List<A>): Boolean =
    when (xs) {
        is Nil -> false
        is Cons ->
            if(startWith(xs, sub)) true
            else hasSubsequence(xs.tail, sub)
    }

sealed class Tree<out A>

data class Leaf<A>(val value: A) : Tree<A>()

data class Branch<A>(val left: Tree<A>, val right: Tree<A>): Tree<A>()

// 3.24
fun <A> size(tree: Tree<A>): Int =
    when (tree) {
        is Leaf -> 1
        is Branch -> 1 + size(tree.left) + size(tree.right)
    }

// 3.25
fun maximum(tree: Tree<Int>): Int {
    fun max(tree: Tree<Int>, maxValue: Int): Int =
        when (tree) {
            is Leaf -> maxOf(maxValue, tree.value)
            is Branch -> maxOf(max(tree.left, maxValue), max(tree.right, maxValue))
        }
    return max(tree, 0)
}

// 3.26
fun depth(tree: Tree<Int>): Int =
    when(tree) {
        is Leaf -> 0
        is Branch -> 1 + maxOf(depth(tree.left), depth(tree.right))
    }

// 3.27
fun <A, B> map(tree: Tree<A>, f: (A) -> B): Tree<B> =
    when(tree) {
        is Leaf -> Leaf(f(tree.value))
        is Branch -> Branch(map(tree.left, f), map(tree.right, f))
    }

// 3.28
fun <A, B> fold(ta: Tree<A>, l: (A) -> B, b: (B, B) -> B): B =
    when (ta) {
        is Leaf -> l(ta.value)
        is Branch -> b(fold(ta.left, l, b), fold(ta.right, l, b))
    }

fun <A> sizeF(ta: Tree<A>): Int =
    fold(ta, {1}, {b1, b2 -> 1 +b1 +b2})

fun maximumF(ta: Tree<Int>): Int =
    fold(ta, { a -> a}, { b1, b2 -> maxOf(b1, b2)})

fun <A> depthF(ta: Tree<A>): Int =
    fold(ta, {0}, {b1, b2 -> 1 + maxOf(b1, b2)})

fun <A, B> mapF(ta: Tree<A>, f: (A) -> B): Tree<B> =
    fold(ta, {a: A -> Leaf(f(a))}, { b1: Tree<B>, b2: Tree<B> -> Branch(b1, b2) })
