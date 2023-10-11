import org.junit.Assert

sealed class List<out A> {
    companion object {
        // 리스트 3.2
        fun <A> of(vararg aa: A): List<A> {
            val tail = aa.sliceArray(1 until aa.size)
            return if (aa.isEmpty()) Nil else Cons(aa[0], of(*tail))
        }

        // 리스트 3.3
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

        // 연습문제 3.1
        fun <A> tail(xs: List<A>): List<A> =
            when (xs) {
                is Nil -> Nil
                is Cons -> xs.tail
            }

        // 연습문제 3.2
        fun <A> setHead(xs: List<A>, x: A): List<A> =
            when (xs) {
                is Nil -> Nil
                is Cons -> Cons(x, xs.tail)
            }

        // 연습문제 3.3
        fun <A> drop(l: List<A>, n: Int): List<A> {
            tailrec fun loop(l: List<A>, n: Int): List<A> =
                if (n <= 0) l
                else when (l) {
                    is Nil -> Nil
                    is Cons -> loop(l.tail, n - 1)
                }
            return loop(l, n)
        }


        // 연습문제 3.4
        fun <A> dropWhile(l: List<A>, f: (A) -> Boolean): List<A> =
            when (l) {
                is Nil -> Nil
                is Cons ->
                    if (f(l.head)) dropWhile(l.tail, f)
                    else Cons(l.head, dropWhile(l.tail, f))
            }

        // 연습문제 3.5
        fun <A> init(l: List<A>): List<A> {
            fun loop(l: List<A>): List<A> =
                when (l) {
                    is Nil -> Nil
                    is Cons ->
                        if (l.tail == Nil) Nil
                        else Cons(l.head, loop(l.tail))
                }
            return loop(l)
        }

        // 리스트 3.11
        fun <A, B> foldRight(xs: List<A>, z: B, f: (A, B) -> B): B =
            when (xs) {
                is Nil -> z
                is Cons -> f(xs.head, foldRight(xs.tail, z, f))
            }
        fun sum2(ints: List<Int>): Int = foldRight(ints, 0, { a, b -> a + b })
        fun product2(dbs: List<Double>): Double = foldRight(dbs, 1.0, { a, b -> a * b })

        // 연습문제 3.7
        fun <A> empty(): List<A> = Nil

        // 연습문제 3.8
        fun <A> length(xs: List<A>): Int = foldRight(xs, 0) { _, b -> b + 1 }

        // 연습문제 3.9
        fun <A, B> foldLeft(xs: List<A>, z: B, f: (B, A) -> B): B {
            tailrec fun loop(xs: List<A>, z: B, f: (B, A) -> B): B =
                when (xs) {
                    is Nil -> z
                    is Cons -> loop(xs.tail, f(z, xs.head), f)
                }
            return loop(xs, z, f)
        }

        // 연습문제 3.10
        fun sumFoldLeft(ints: List<Int>): Int = foldLeft(ints, 0, { b, a -> b + a })
        fun productFoldLeft(dbs: List<Double>): Double = foldLeft(dbs, 1.0, { b, a -> b * a })
        fun <A> lengthFoldLeft(xs: List<A>): Int = foldLeft(xs, 0, { b, _ -> b + 1 })

        // 연습문제 3.13
        fun <A> append(xs: List<A>, item: A): List<A> = foldRight(
            xs,
            List.of(item),
            { a, b -> Cons(a, b) }
        )

        // 연습문제 3.14
        fun <A> compactMap(xs: List<List<A>>): List<A> = foldRight(
            xs,
            empty(),
            { list1, list2 -> foldRight(list1, list2, { a, b -> Cons(a, b) }) }
        )

        // 연습문제 3.15
        fun plusOne(xs: List<Int>): List<Int> = foldRight(
            xs,
            empty(),
            { a, b -> Cons(a + 1, b) }
        )

        // 연습문제 3.17
        fun <A, B> map(xs: List<A>, f: (A) -> B): List<B> = foldRight(
            xs,
            empty(),
            { a, b -> Cons(f(a), b) }
        )

        // 연습문제 3.18
        fun <A> filter(xs: List<A>, f: (A) -> Boolean): List<A> {
            fun loop(xs: List<A>): List<A> =
                when (xs) {
                    is Nil -> Nil
                    is Cons ->
                        if (f(xs.head)) Cons(xs.head, loop(xs.tail))
                        else loop(xs.tail)
                }
            return loop(xs)
        }
    }

}
// 연습문제 3.16
fun List<Double>.toStringList(): List<String> = List.foldRight(this, List.empty(), { a, b -> Cons(a.toString(), b)})

object Nil: List<Nothing>()
data class Cons<out A>(val head: A, val tail: List<A>) : List<A>()

sealed class Tree<out A> {
    companion object {
        // 연습문제 3.24
        fun <A> size(tr: Tree<A>): Int {
            fun loop(tr: Tree<A>, acc: Int): Int =
                when (tr) {
                    is Leaf -> acc + 1
                    is Branch -> loop(tr.left, acc) + loop(tr.right, acc) + 1
                }
            return loop(tr, 0)
        }

        // 연습문제 3.25
        fun maximum(tr: Tree<Int>): Int {
            fun loop(tr: Tree<Int>, mx: Int): Int =
                when (tr) {
                    is Leaf -> max(mx, tr.value)
                    is Branch -> max(max(loop(tr.left, mx),loop(tr.right, mx)), 1)
                }
            return loop(tr, Int.MIN_VALUE)
        }

        // 연습문제 3.26
        fun depth(tr: Tree<Int>): Int {
            fun loop(tr: Tree<Int>, d: Int): Int =
                when (tr) {
                    is Leaf -> d + 1
                    is Branch -> max(loop(tr.left, d + 1), loop(tr.right, d + 1))
                }
            return loop(tr, -1)
        }

        // 연습문제 3.27
        fun <A, B> map(tr: Tree<A>, f: (A) -> B): Tree<B> {
            fun loop(tr: Tree<A>): Tree<B> =
                when (tr) {
                    is Leaf -> Leaf(f(tr.value))
                    is Branch -> Branch(loop(tr.left), loop(tr.right))
                }
            return loop(tr)
        }

        // 연습문제 3.28
        fun <A, B> fold(ta: Tree<A>, l: (A) -> B, b: (B, B) -> B): B =
            when (ta) {
                is Leaf -> l(ta.value)
                is Branch -> b(fold(ta.left, l, b), fold(ta.right, l, b))
            }

        fun <A> sizeF(ta: Tree<A>): Int = fold(
            ta,
            { 1 },
            { a, b -> a + b + 1 }
        )
        fun maximumF(ta: Tree<Int>): Int = fold(
            ta,
            { it },
            { a, b -> max(max(a, b), 1) }
        )
        fun <A> depthF(ta: Tree<A>): Int = fold(
            ta,
            { 0 },
            { a, b -> max(a, b) + 1 }
        )
        fun <A, B> mapF(ta: Tree<A>, f: (A) -> B): Tree<B> = fold(
            ta,
            { Leaf(f(it)) },
            { a: Tree<B>, b: Tree<B> -> Branch(a, b) }
        )
    }
}
data class Leaf<A>(val value: A): Tree<A>()
data class Branch<A>(val left: Tree<A>, val right: Tree<A>): Tree<A>()

fun main(args: Array<String>) {
    println("Hello World!")

    // 연습문제 3.1
    val practice3_1: List<Int> = List.of(1,2,3,4,5)
    Assert.assertEquals(List.tail(practice3_1), List.of(2,3,4,5))
    Assert.assertEquals(List.tail(Nil), Nil)

    // 연습문제 3.2
    val practice3_2: List<Int> = List.of(1,2,3,4,5)
    Assert.assertEquals(List.setHead(practice3_2, 7), List.of(7,2,3,4,5))
    Assert.assertEquals(List.setHead(Nil, 7), Nil)

    // 연습문제 3.3
    val practice3_3_1: List<Int> = List.of(1,2,3,4,5)
    Assert.assertEquals(List.drop(practice3_3_1,1), List.of(2,3,4,5))
    val practice3_3_2: List<Int> = List.of(1,2,3,4,5)
    Assert.assertEquals(List.drop(practice3_3_2,2), List.of(3,4,5))
    Assert.assertEquals(List.drop(Nil,1), Nil)

    // 연습문제 3.4
    val practice3_4_1: List<Int> = List.of(1,2,3,4,5)
    Assert.assertEquals(List.dropWhile(practice3_4_1, { it % 2 == 0 }), List.of(1,3,5))
    val practice3_4_2: List<Int> = List.of(1,2,3,4,5)
    Assert.assertEquals(List.dropWhile(practice3_4_2, { it % 3 == 0 }), List.of(1,2,4,5))
//    Assert.assertEquals(List.dropWhile(Nil, { it % 3 == 0 }), Nil) // 왜 안되지..?

    // 연습문제 3.5
    val practice3_5_1: List<Int> = List.of(1,2,3,4,5)
    Assert.assertEquals(List.init(practice3_5_1), List.of(1,2,3,4))

    // 연습문제 3.7
    Assert.assertEquals(
        List.foldRight(
            Cons(1, Cons(2, Cons(3, Nil))),
            Nil as List<Int>,
            { x, y -> Cons(x, y) }
        ), List.foldRight(
            Nil as List<Int>,
            Cons(1, Cons(2, Cons(3, Nil))),
            { x, y -> Cons(x, y) }
        )
    )

    // 연습문제 3.8
    val practice3_8: List<Int> = List.of(1,2,3,4,5)
    Assert.assertEquals(List.length(practice3_8), 5)

    // 연습문제 3.10
    val practice3_10_1: List<Int> = List.of(1,2,3,4,5)
    val practice3_10_2: List<Double> = List.of(1.0, 2.0, 3.0)
    Assert.assertEquals(List.sumFoldLeft(practice3_10_1), 15)
    Assert.assertEquals(List.productFoldLeft(practice3_10_2), 6.0, 1.0)
    Assert.assertEquals(List.lengthFoldLeft(practice3_10_2), 3)

    // 연습문제 3.13
    val practice3_13: List<Int> = List.of(1,2,3,4,5)
    Assert.assertEquals(List.append(practice3_13, 6), List.of(1,2,3,4,5,6))
    Assert.assertEquals(List.append(Nil, 1), List.of(1))

    // 연습문제 3.14
    val practice3_14: List<List<Int>> = List.of(List.of(1,2,3,4,5), List.of(6,7))
    Assert.assertEquals(List.compactMap(practice3_14), List.of(1,2,3,4,5,6,7))

    // 연습문제 3.15
    val practice3_15: List<Int> = List.of(1,2,3,4,5)
    Assert.assertEquals(List.plusOne(practice3_15), List.of(2,3,4,5,6))

    // 연습문제 3.16
    val practice3_16: List<Double> = List.of(1.0, 2.0, 3.0, 4.0, 5.0)
    Assert.assertEquals(practice3_16.toStringList(), List.of("1.0", "2.0", "3.0", "4.0", "5.0"))

    // 연습문제 3.17
    val practice3_17: List<Double> = List.of(1.0, 2.0, 3.0, 4.0, 5.0)
    Assert.assertEquals(List.map(practice3_17, { it.toString() }), List.of("1.0", "2.0", "3.0", "4.0", "5.0"))

    // 연습문제 3.18
    val practice3_18: List<Int> = List.of(1,2,3,4,5)
    Assert.assertEquals(List.filter(practice3_18, { it % 2 == 0 }), List.of(2, 4))

    // 연습문제 3.24
    val practice3_24_1: Tree<Int> = Branch(Branch(Leaf(1), Leaf(2)), Leaf(3))
    Assert.assertEquals(Tree.size(practice3_24_1), 5)
    val practice3_24_2: Tree<Int> = Branch(Branch(Leaf(1), Leaf(2)), Branch(Leaf(3), Leaf(4)))
    Assert.assertEquals(Tree.size(practice3_24_2), 7)

    // 연습문제 3.25
    val practice3_25: Tree<Int> = Branch(Branch(Leaf(7), Leaf(9)), Leaf(3))
    Assert.assertEquals(Tree.maximum(practice3_25), 9)

    // 연습문제 3.26
    val practice3_26_1: Tree<Int> = Branch(Branch(Leaf(7), Leaf(9)), Leaf(3))
    Assert.assertEquals(Tree.depth(practice3_26_1), 2)
    val practice3_26_2: Tree<Int> = Branch(Branch(Leaf(1), Branch(Branch(Leaf(2), Leaf(3)), Leaf(4))), Leaf(5))
    Assert.assertEquals(Tree.depth(practice3_26_2), 4)

    // 연습문제 3.27
    val practice3_27: Tree<Int> = Branch(Branch(Leaf(1), Leaf(2)), Leaf(3))
    Assert.assertEquals(Tree.map(practice3_27, { it * 2 }), Branch(Branch(Leaf(2), Leaf(4)), Leaf(6)))

    // 연습문제 3.28
    val practice3_28_size_1: Tree<Int> = Branch(Branch(Leaf(1), Leaf(2)), Leaf(3))
    Assert.assertEquals(Tree.sizeF(practice3_28_size_1), 5)
    val practice3_28_size_2: Tree<Int> = Branch(Branch(Leaf(1), Leaf(2)), Branch(Leaf(3), Leaf(4)))
    Assert.assertEquals(Tree.sizeF(practice3_28_size_2), 7)
    val practice3_28_maximum: Tree<Int> = Branch(Branch(Leaf(7), Leaf(9)), Leaf(3))
    Assert.assertEquals(Tree.maximumF(practice3_28_maximum), 9)
    val practice3_28_depth_1: Tree<Int> = Branch(Branch(Leaf(7), Leaf(9)), Leaf(3))
    Assert.assertEquals(Tree.depthF(practice3_28_depth_1), 2)
    val practice3_28_depth_2: Tree<Int> = Branch(Branch(Leaf(1), Branch(Branch(Leaf(2), Leaf(3)), Leaf(4))), Leaf(5))
    Assert.assertEquals(Tree.depthF(practice3_28_depth_2), 4)
    val practice3_28_map: Tree<Int> = Branch(Branch(Leaf(1), Leaf(2)), Leaf(3))
    Assert.assertEquals(Tree.mapF(practice3_28_map, { it * 2 }), Branch(Branch(Leaf(2), Leaf(4)), Leaf(6)))
}