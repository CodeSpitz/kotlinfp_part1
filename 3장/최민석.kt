
import kotlin.math.max

sealed class ListE<out A>

object Nil: ListE<Nothing>()

data class Cons<out A>(val head: A, val tail: ListE<A>): ListE<A>()


// ex 3.1
fun <A> tail(xs: ListE<A>): ListE<A> = when(xs) {
    is Cons -> xs.tail
    Nil -> Nil
}

// ex 3.2
fun <A> setHead(xs: ListE<A>, x: A): ListE<A> = Cons(x, tail(xs))

// ex 3.3
tailrec fun <A> drop(l: ListE<A>, n: Int): ListE<A> = when(n) {
    0 -> l
    else -> drop(tail(l), n-1)
}

// ex 3.4
tailrec fun <A> dropWhile(l: ListE<A>, f: (A) -> Boolean): ListE<A> = when(l) {
    Nil -> Nil
    is Cons -> {
        if (f(l.head)) dropWhile(l.tail, f)
        else l
    }
}

// ex 3.5
fun <A> init(l:ListE<A>): ListE<A> = when(l) {
    Nil -> l
    is Cons -> when(tail(l)){
        Nil -> Nil
        is Cons -> Cons(l.head, init(l.tail))
    }
}

// ex 3.6
// 1. 현 구조로는 불가능. 내부 로직 수정 필요.


// ex 3.7
// foldRight은 ListE의 데이터구조에 강하게 의존하고 있음.

// ex 3.8
fun <A, B> foldRight( xs: ListE<A>, z: B, f: (A, B) -> B): B = when(xs) {
    is Nil -> z
    is Cons -> f(xs.head, foldRight(xs.tail, z, f))
}

fun <A> lengthFoldRight(xs: ListE<A>): Int = foldRight(xs, 0) { _, b -> b + 1}

// ex 3.9
tailrec fun <A, B> foldLeft(xs: ListE<A>, z: B, f: (B, A) -> B): B = when(xs) {
    Nil -> z
    is Cons -> foldLeft(xs.tail, f(z, xs.head), f)
}

// ex 3.10
fun sumFoldLeft(xs: ListE<Int>): Int = foldLeft(xs, 0) { b, a -> b + a}
fun productFoldLeft(xs: ListE<Int>): Double = foldLeft(xs, 1.0) { b, a -> b * a}
fun <A> lengthFoldLeft(xs: ListE<A>): Int = foldLeft(xs, 0) { b, _ -> b + 1}

// ex 3.11
fun <A> reverse(xs: ListE<A>) = foldLeft(xs, Nil as ListE<A>) { b, a -> Cons(a, b)}

// ex 3.12
fun <A, B> foldLeft2(xs: ListE<A>, z: B, f: (B, A) -> B): B = foldRight(
    xs,
    z = { b: B -> b },
    f = { a, g ->
        { b ->
            g(f(b, a))
        }
    }
)(z)

// ex 3.13
fun <A> appendR(a1: ListE<A>, a2: ListE<A>): ListE<A> = foldRight(a1, a2) {
    a, b -> Cons(a, b)
}

// ex 3.14
fun <A> concat(a: ListE<ListE<A>>): ListE<A> = foldRight(a, Nil as ListE<A>) { a, b -> appendR(a, b) }

// ex 3.15
fun increaseIntBy1(xs: ListE<Int>): ListE<Int> = when(xs) {
    Nil -> Nil
    is Cons -> Cons(xs.head + 1 , increaseIntBy1( xs.tail))
}

// ex 3.16
fun doubleToString(xs: ListE<Double>): ListE<String> = when(xs) {
    Nil -> Nil
    is Cons -> Cons(xs.head.toString(), doubleToString(xs.tail) )
}

// ex 3.17
fun <A, B> map(xs: ListE<A>, f: (A) -> B): ListE<B> = when(xs) {
    Nil -> Nil
    is Cons -> Cons(f(xs.head), map(xs.tail, f))
}

// ex 3.18
fun <A> filter(xs: ListE<A>, f: (A) -> Boolean): ListE<A> = when(xs) {
    Nil -> Nil
    is Cons -> if (f(xs.head)) Cons(xs.head, filter(xs.tail, f)) else filter(xs.tail, f)
}

// ex 3.19
fun <A, B> flatMap(xa: ListE<A>, f: (A) -> ListE<B>): ListE<B> {
   return  when(xa) {
        Nil -> Nil
        is Cons -> appendR(f(xa.head), flatMap(xa.tail, f))
    }
}


// ex 3.20
fun <A> filterFlatMap(xs: ListE<A>, f: (A) -> Boolean): ListE<A> = flatMap(xs) {
    if (f(it)) Cons(it, Nil)
    else Nil
}

// ex 3.21
fun <A> addListByElement(a: ListE<Int>, b: ListE<Int>): ListE<Int> = when(a) {
    Nil -> Nil
    is Cons -> when(b) {
        Nil -> Cons(a.head, addListByElement<Int>(a.tail, b))
        is Cons -> Cons(a.head + b.head, addListByElement<Int>(a.tail, b.tail))
    }
}

// ex 3.22
fun <A, B> addListByElement(a: ListE<A>, b: ListE<A>, z: (A, A?) -> B): ListE<B> = when(a) {
    Nil -> Nil
    is Cons -> when(b) {
        Nil -> Cons(z(a.head, null), addListByElement(a.tail, b, z))
        is Cons -> Cons(z(a.head, b.head), addListByElement(a.tail, b.tail, z))
    }
}


// ex 3.23
tailrec fun <A> hasSubsequence(xs: ListE<A>, sub: ListE<A>): Boolean {
    tailrec fun <B> subloop(xs: ListE<B>, sub: ListE<B>): Boolean = when(sub) {
        Nil -> true
        is Cons -> {
            when(xs) {
                Nil -> false
                is Cons -> if (xs.head == sub.head) subloop(xs.tail, sub.tail) else false
            }
        }
    }

    return when(xs) {
        Nil -> false
        is Cons -> if (subloop(xs.tail, sub)) true else hasSubsequence(xs.tail, sub)
    }
}


sealed class Tree<out A>
data class Leaf<A>(val value: A): Tree<A>()
data class Branch<A>(val left: Tree<A>, val right: Tree<A>): Tree<A>()

// 3.24
fun <A> sizeOfTree(t: Tree<A>): Int = when(t) {
    is Leaf -> 1
    is Branch -> 1 + sizeOfTree(t.left) + sizeOfTree(t.right)
}

// 3.25
fun maxOfTree(t: Tree<Int>): Int  = when(t) {
    is Leaf -> t.value
    is Branch -> max(maxOfTree(t.left), maxOfTree(t.right))
}

// 3.26
fun <A> depthOfTree(t: Tree<A>): Int = when(t) {
    is Leaf -> 1
    is Branch -> max(depthOfTree(t.left) + 1, depthOfTree(t.right) + 1)
}

// 3.27
fun <A, B> mapTree(t: Tree<A>, f: (A) -> B): Tree<B> = when(t) {
    is Leaf -> Leaf(f(t.value))
    is Branch -> Branch(mapTree(t.left, f), mapTree(t.right, f))
}

// 3.28
fun <A, B> fold(ta: Tree<A>, l: (A) -> B, b: (B, B) -> B): B = when(ta) {
    is Leaf -> l(ta.value)
    is Branch -> b(fold(ta.left, l, b), fold(ta.right, l, b))
}

fun <A> sizeF(ta: Tree<A>): Int = fold(ta, l = { 1 }) { b1, b2 -> 1 + b1 + b2 }

fun maximumF(ta: Tree<Int>): Int = fold(ta, l = { it }) { b1, b2 -> max(b1, b2)  }

fun <A> depthF(ta: Tree<A>): Int = fold(ta, l = { 1 }) { b1, b2 -> max(b1 + 1, b2 + 1)}

fun <A, B> mapF(ta: Tree<A>, f: (A) -> B): Tree<B> = fold<A, Tree<B>>(ta, l = { Leaf(f(it)) }) { b1, b2 -> Branch(b1, b2)}
















