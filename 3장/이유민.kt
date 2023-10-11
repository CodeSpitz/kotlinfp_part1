sealed class List<out A> {
    companion object {
        fun <A> of(vararg aa: A): List<A> {
            val tail = aa.sliceArray(1 until aa.size)
            return if (aa.isEmpty()) Nil else Cons(aa[0], of(*tail))
        }
    }
}

object Nil : List<Nothing>()

data class Cons<out A>(
    val head: A,
    val tail: List<A>,
) : List<A>()

// 3.1
fun <A> tail(xs: List<A>): List<A> = when (xs) {
    is Nil -> xs
    is Cons -> xs.tail
}

// 3.2
fun <A> setHead(xs: List<A>, x: A): List<A> = when (xs) {
    is Nil -> Cons(x, Nil)
    is Cons -> Cons(x, xs.tail)
}

// 3.3
fun <A> drop(xs: List<A>, n: Int): List<A> = when (xs) {
    is Nil -> xs
    is Cons -> if (n == 0) xs else drop(xs.tail, n - 1)
}

// 3.4
fun <A> dropWhile(l: List<A>, f: (A) -> Boolean): List<A> = when (l) {
    is Nil -> l
    is Cons -> if (f(l.head)) dropWhile(l.tail, f) else l
}

// 3.5
fun <A> init(l: List<A>): List<A> = when (l) {
    is Nil -> l
    is Cons -> if (l.tail is Nil) Nil else Cons(l.head, init(l.tail))
}

fun <A, B> foldRight(l: List<A>, z: B, f: (A, B) -> B): B = when (l) {
    is Nil -> z
    is Cons -> f(l.head, foldRight(l.tail, z, f))
}

// 3.8
fun <A> length(l: List<A>): Int = foldRight(l, 0, { _, x -> x + 1 })

// 3.9
fun <A, B> foldLeft(l: List<A>, z: B, f: (B, A) -> B): B = when (l) {
    is Nil -> z
    is Cons -> foldLeft(l.tail, f(z, l.head), f)
}

// 3.10
fun leftSum(xs: List<Int>): Int = foldLeft(xs, 0, { x, y -> x + y })
fun leftProduct(xs: List<Double>): Double = foldLeft(xs, 1.0, { x, y -> x * y })
fun leftLength(xs: List<Int>): Int = foldLeft(xs, 0, { x, _ -> x + 1 })

// 3.11
fun <A> reverse(xs: List<A>): List<A> = foldLeft(xs, Nil as List<A>, { x, y -> Cons(y, x) })

// 3.12
fun <A, B> foldLeft2(l: List<A>, z: B, f: (B, A) -> B): B = foldRight(
    l,
    { b: B -> b },
    { a, g -> { b -> g(f(b, a)) } },
)(z)
fun <A, B> foldRight2(l: List<A>, z: B, f: (A, B) -> B): B = foldLeft(
    l,
    { b: B -> b },
    { g, a -> { b -> g(f(a, b)) } },
)(z)

// 3.13
fun <A> append(a1: List<A>, a2: List<A>): List<A> = foldRight(a1, a2, { x, y -> Cons(x, y) })

// 3.14
fun <A> concat(l: List<List<A>>): List<A> = foldRight(l, Nil as List<A>, { x, y -> append(x, y) })

// 3.15
fun addOne(xs: List<Int>): List<Int> = foldRight(xs, Nil as List<Int>, { x, y -> Cons(x + 1, y) })

// 3.16
fun doubleToString(xs: List<Double>): List<String> =
    foldRight(xs, Nil as List<String>, { x, y -> Cons(x.toString(), y) })

// 3.17
fun <A, B> map(xs: List<A>, f: (A) -> B): List<B> = foldRight(xs, Nil as List<B>, { x, y -> Cons(f(x), y) })

// 3.18
fun <A> filter(xs: List<A>, f: (A) -> Boolean): List<A> = foldRight(xs, Nil as List<A>, { x, y -> if (f(x)) Cons(x, y) else y })

// 3.19
fun <A, B> flatMap(xa: List<A>, f: (A) -> List<B>): List<B> = concat(map(xa, f))

// 3.20
fun <A> filter2(xs: List<A>, f: (A) -> Boolean): List<A> = flatMap(xs, { x -> if (f(x)) List.of(x) else Nil })

// 3.21
fun zipAdd(xs: List<Int>, ys: List<Int>): List<Int> = when (xs) {
    is Nil -> Nil
    is Cons -> when (ys) {
        is Nil -> Nil
        is Cons -> Cons(xs.head + ys.head, zipAdd(xs.tail, ys.tail))
    }
}

// 3.22
fun <A, B> zipWith(xs: List<A>, ys: List<A>, f: (A, A) -> B): List<B> = when (xs) {
    is Nil -> Nil
    is Cons -> when (ys) {
        is Nil -> Nil
        is Cons -> Cons(f(xs.head, ys.head), zipWith(xs.tail, ys.tail, f))
    }
}

// 3.23
tailrec fun <A> startsWith(l1: List<A>, l2: List<A>): Boolean = when (l1) {
    is Nil -> true
    is Cons -> when (l2) {
        is Nil -> true
        is Cons -> if (l1.head == l2.head) startsWith(l1.tail, l2.tail) else false
    }
}

tailrec fun <A> hasSubsequence(xs: List<A>, sub: List<A>): Boolean = when (xs) {
    is Nil -> false
    is Cons -> if (startsWith(xs, sub)) true else hasSubsequence(xs.tail, sub)
}

// tree
sealed class Tree<out A>

data class Leaf<A>(val value: A) : Tree<A>()

data class Branch<A>(val left: Tree<A>, val right: Tree<A>) : Tree<A>()

// 3.24
fun <A> size(t: Tree<A>): Int = when (t) {
    is Leaf -> 1
    is Branch -> size(t.left) + size(t.right)
}

// 3.25
fun maximum(t: Tree<Int>): Int = when (t) {
    is Leaf -> t.value
    is Branch -> maxOf(maximum(t.left), maximum(t.right))
}

// 3.26
fun <A> depth(t: Tree<A>): Int = when (t) {
    is Leaf -> 1
    is Branch -> maxOf(depth(t.left), depth(t.right)) + 1
}

// 3.27
fun <A, B> map(t: Tree<A>, f: (A) -> B): Tree<B> = when (t) {
    is Leaf -> Leaf(f(t.value))
    is Branch -> Branch(map(t.left, f), map(t.right, f))
}

// 3.28
fun <A, B> fold(t: Tree<A>, l: (A) -> B, b: (B, B) -> B): B =
    when (t) {
        is Leaf -> l(t.value)
        is Branch -> b(fold(t.left, l, b), fold(t.right, l, b))
    }

fun <A> sizeF(t: Tree<A>): Int = fold(t, { 1 }, { a, b -> a + b + 1 })

fun <A> maximumF(t: Tree<Int>): Int = fold(t, { it }, { a, b -> maxOf(a, b) })

fun <A> depthF(t: Tree<A>): Int = fold(t, { 1 }, { a, b -> maxOf(a, b) + 1 })

fun <A, B> mapF(t: Tree<A>, f: (A) -> B): Tree<B> = fold(t, { a: A -> Leaf(f(a)) }, { a: Tree<B>, b: Tree<B> -> Branch(a, b) })
