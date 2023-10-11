import org.graalvm.compiler.asm.sparc.SPARCAssembler.Br

fun main(args: Array<String>) {
    val of = List.of(1, 2, 3, 4, 5)
    val dbof = List.of(1.0, 2.0, 3.0, 4.0)

    println(" List.tail(of) - ${List.tail(of)}")
    println(" List.setHead(of, 5) - ${List.setHead(of, 5)}")
    println(" List.drop(of, 3) - ${List.drop(of, 3)}")
    println(" List.dropWhile(of) { a -> a == 1 || a == 2  } - ${List.dropWhile(of) { a -> a == 1 || a == 2  }}")
    println(" List.append(of, List.of(5, 6, 7, 8)) - ${List.append(of, List.of(5, 6, 7, 8))}")
    println(" List.init(of) - ${List.init(of)}")
    println(" List.init(of) - ${List.foldRight(of, 0) { a, b -> a + b }}")
    println(" List.foldRightPassNilAndCons() - ${List.foldRightPassNilAndCons()}")
    println(" List.length(of) - ${List.length(of)}")
    println(" List.foldLeft(of, 0) {a, b -> a + b} - ${List.foldLeft(of, 0) {a, b -> a + b}}")
    println(" List.sum3(of) - ${List.sum3(of)}")
    println(" List.product3(dbof) - ${List.product3(dbof)}")
    println(" List.length3(dbof) - ${List.length3(dbof)}")
    println(" List.reverseList(dbof) - ${List.reverseList(dbof)}")
    println(" List.foldLeftR(of, 0) {a, b -> a + b} - ${List.foldLeftR(of, 0) {a, b -> a + b}}")
    println(" List.appendByFold(of, List.of(6, 7, 8, 9, 10)) - ${List.appendByFold(of, List.of(6, 7, 8, 9, 10))}")
    println(" List.of(of, of, of) - ${List.of(of, of, of)}")
    println(" List.concat(List.of(of, of, of)) - ${List.concat(List.of(of, of, of))}")
    println(" List.concat2(List.of(of, of, of)) - ${List.concat2(List.of(of, of, of))}")
    println(" List.increase(of) - ${List.increase(of)}")
    println(" List.doubleToString(dbof) - ${List.doubleToString(dbof)}")
    println(" List.map(of) {a -> a.toString().plus(\"!\")} - ${List.map(of) {a -> a.toString().plus("!")}}")
    println(" List.filter(of) {a -> a % 2 == 1} - ${List.filter(of) {a -> a % 2 == 1}}")
    println(" List.flatMap(of) { i -> List.of(i, i)} - ${List.flatMap(of) { i -> List.of(i, i)}}")
    println(" List.filterByFlatMap(of) {a -> a % 2 == 1} - ${List.filterByFlatMap(of) {a -> a % 2 == 1}}")
    println(" List.addSamePositionInList(of, List.of(1, 2, 3, 4, 5)) - ${List.addSamePositionInList(of, List.of(1, 2, 3, 4, 5)) }")
    println(" List.zipWith(of, List.of(1, 2, 3, 4, 5)) { a, b -> a + b} - ${List.zipWith(of, List.of(1, 2, 3, 4, 5)) { a, b -> a + b}}")
    println(" List.startWith(of, List.of(1, 2, 3, 4, 5)) - ${List.startWith(of, List.of(5, 4, 3, 2, 1))}")
    println(" List.hasSubsequence(of, List.of(5, 4, 3, 2, 1)) - ${List.hasSubsequence(of, List.of(1, 2, 3, 4, 5))}")

    println(" Tree.size(Branch(Leaf(1), Branch(Leaf(2), Leaf(3)))) - ${Tree.size(Branch(Leaf(1), Branch(Leaf(2), Leaf(3))))}")
}

sealed class List<out A> {
    companion object {
        fun <A> of(vararg aa: A): List<A> {
            val tail: Array<out A> = aa.sliceArray(1 until aa.size)
            return if (aa.isEmpty()) Nil else Cons(aa[0], of(*tail))
        }

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
        fun <A> tail(xs: List<A>): List<A> =
            when (xs) {
                is Nil -> Nil
                is Cons -> xs.tail
            }

        // 3.2
        fun <A> setHead(xs: List<A>, x: A): List<A> =
            when (xs) {
                is Nil -> Nil
                is Cons -> Cons(x, xs.tail)
            }

        // 3.3
        fun <A> drop(l: List<A>, n: Int): List<A> =
            if (n == 0) {
                l
            } else {
                when (l) {
                    is Nil -> Nil
                    is Cons -> drop(l.tail, n - 1)
                }
            }

        // 3.4
        fun <A> dropWhile(l: List<A>, f: (A) -> Boolean): List<A> =
            when (l) {
                is Nil -> Nil
                is Cons -> if (f(l.head)) dropWhile(l.tail, f) else l
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
                is Cons -> {
                    if (l.tail == Nil) {
                        Nil
                    } else {
                        Cons(l.head, init(l.tail))
                    }
                }
            }

        fun <A, B> foldRight(xs: List<A>, z: B, f: (A, B) -> B): B =
            when (xs) {
                is Nil -> z
                is Cons -> f(xs.head, foldRight(xs.tail, z, f))
            }

        // 3.6
        // X.

        // 3.7
        fun foldRightPassNilAndCons(): List<Int> {
            return foldRight(Cons(1, Cons(2, Cons(3, Nil))), empty()) { x, y -> Cons(x, y) }
        }

        fun <A> empty(): List<A> = Nil

        // 3.8
        fun <A> length(xs: List<A>): Int =
            foldRight(xs, 0) { a, b -> 1 + b }

        // 3.9
        tailrec fun <A, B> foldLeft(xs: List<A>, z: B, f: (B, A) -> B): B =
            when (xs) {
                is Nil -> z
                is Cons -> foldLeft(xs.tail, f(z, xs.head), f)
            }

        // 3.10
        fun sum3(ints: List<Int>): Int = foldLeft(ints, 0) { b, a -> b + a }
        fun product3(dbs: List<Double>): Double = foldLeft(dbs, 1.0) { b, a -> b * a }
        fun <A> length3(xs: List<A>): Int = foldLeft(xs, 0) { b, a -> 1 + b }

        // 3.11
        fun <A> reverseList(xs: List<A>): List<A> =
            foldLeft(xs, empty()) { a: List<A>, b: A -> Cons(b, a) }

        // 3.12 X
        fun <A, B> foldLeftR(xs: List<A>, z: B, f: (B, A) -> B): B =
            foldRight(xs, { b: B -> b }, { a, g -> { b -> g(f(b, a)) } })(z)

        // 3.13
        fun <A> appendByFold(a1: List<A>, a2: List<A>): List<A> =
            foldRight(a1, a2) { a, b -> Cons(a, b) }

        // 3.14 X
        fun <A> concat(xxs: List<List<A>>): List<A> =
            foldRight(xxs, empty()) { xs1: List<A>, xs2: List<A> -> foldRight(xs1, xs2) { a, ls -> Cons(a, ls) } }

        fun <A> concat2(xxs: List<List<A>>): List<A> =
            foldRight(xxs, empty()) { xs1: List<A>, xs2: List<A> -> append(xs1, xs2) }

        // 3.15
        fun increase(xs: List<Int>): List<Int> =
            foldRight(xs, empty()) { a: Int, b -> Cons(a + 1, b) }

        // 3.16
        fun doubleToString(xs: List<Double>): List<String> =
            foldRight(xs, empty()) { a, b -> Cons(a.toString(), b) }

        // 3.17
        fun <A, B> map(xs: List<A>, f: (A) -> B): List<B> =
            foldRight(xs, empty()) { a, b -> Cons(f(a), b) }

        // 3.18
        fun <A> filter(xs: List<A>, f: (A) -> Boolean): List<A> =
            foldRight(xs, empty()) { a, b ->
                if (f(a)) Cons(a, b)
                else b
            }

        // 3.19 X
        fun <A, B> flatMap(xa: List<A>, f: (A) -> List<B>): List<B> =
            foldRight(xa, empty()) { a, lb -> append(f(a), lb) }

        // 3.20
        fun <A> filterByFlatMap(xs: List<A>, f: (A) -> Boolean): List<A> =
            flatMap(xs) { a -> if (f(a)) of(a) else empty() }

        // 3.21 X
        fun addSamePositionInList(xs: List<Int>, xa: List<Int>): List<Int> =
            when (xs) {
                is Nil -> Nil
                is Cons -> {
                    when (xa) {
                        is Nil -> Nil
                        is Cons -> Cons(xs.head + xa.head, addSamePositionInList(xs.tail, xa.tail))
                    }
                }
            }

        // 3.22 X
        fun <A> zipWith(xa: List<A>, xb: List<A>, f: (A, A) -> A): List<A> =
            when(xa) {
                is Nil -> Nil
                is Cons -> when(xb) {
                    is Nil -> Nil
                    is Cons -> Cons(f(xa.head, xb.head), zipWith(xa.tail, xb.tail, f))
                }
            }

        // 3.23 X
        tailrec fun <A> startWith(l1: List<A>, l2: List<A>): Boolean =
            when (l1) {
                is Nil -> l2 == Nil
                is Cons -> when (l2) {
                    is Nil -> true
                    is Cons -> {
                        if (l1.head == l2.head) startWith(l1.tail, l2.tail)
                        else false
                    }
                }
            }

        tailrec fun <A> hasSubsequence(xs: List<A>, sub: List<A>): Boolean =
            when(xs) {
                is Nil -> false
                is Cons -> if ( startWith(xs, sub)) true else hasSubsequence(xs.tail, sub)
            }
    }
}

object Nil: List<Nothing>()
data class Cons<out A>(val head: A, val tail: List<A>): List<A>()

sealed class Tree<out A> {
    companion object {
        // 3.24 X
        fun <A> size(tree: Tree<A>): Int =
            when (tree) {
                is Leaf -> 1
                is Branch -> 1 + size(tree.left) + size(tree.right)
            }

        // 3.25 X
        // 3.26 X
        fun depth(tree: Tree<Int>): Int =
            when (tree) {
                is Leaf -> 0
                is Branch -> 1 + maxOf(depth(tree.left), depth(tree.right))
            }

        // 3.27 X
        fun <A, B> map(tree: Tree<A>, f: (A) -> B): Tree<B> =
            when (tree) {
                is Leaf -> Leaf(f(tree.value))
                is Branch -> Branch(map(tree.left, f), map(tree.right, f))
            }

        // 3.28
        fun <A, B> fold(ta: Tree<A>, l: (A) -> B, b:(B, B) -> B): B =
            when(ta) {
                is Leaf -> l(ta.value)
                is Branch -> b(fold(ta.left, l, b), fold(ta.right, l, b))
            }

        fun <A> sizeF(ta: Tree<A>): Int =
            fold(ta, {1}, {b1, b2 -> 1 + b1 + b2})

        fun maximumf(ta: Tree<Int>): Int =
            fold(ta, {a -> a}, {b1, b2 -> maxOf(b1, b2)})

        fun <A> depthf(ta: Tree<A>): Int =
            fold(ta, {0}, {b1, b2 -> 1 + maxOf(b1, b2)})

        fun <A, B> mapF(ta: Tree<A>, f: (A) -> B): Tree<B> =
            fold(ta, { a:A -> Leaf(f(a))}, {b1:Tree<B>, b2: Tree<B> -> Branch(b1, b2)})

    }
}
data class Leaf<A>(val value: A): Tree<A>()
data class Branch<A>(val left: Tree<A>, val right: Tree<A>): Tree<A>()
