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
}