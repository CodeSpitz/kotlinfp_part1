package codespitz_study_kotlinfunctionalprogramming.`2장`

import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.shouldBe

// 연습문제 2.1
class Exercise1 : WordSpec({
    fun fib(i: Int): Int {
        tailrec fun fibonacciTail(n: Int, a: Int, b: Int): Int {
            if (n == 0) {
                return a
            }

            return fibonacciTail(n - 1, b, a + b);
        }

        return fibonacciTail(i, 0, 1)
    }

    "fib" should {
        "!return the nth fibonacci number" {
            mapOf(
                1 to 1,
                2 to 1,
                3 to 2,
                4 to 3,
                5 to 5,
                6 to 8,
                7 to 13,
                8 to 21
            ).forEach { (n, num) ->
                fib(n) shouldBe num
            }
        }
    }
})

// 연습문제 2.2
val <T> List<T>.tail: List<T>
    get() = drop(1)

val <T> List<T>.head: T
    get() = first()

tailrec fun <A> isSorted(aa: List<A>, order: (A, A) -> Boolean): Boolean {
    if (aa.tail.isEmpty()) {
        return true
    }

    if (!order(aa.head, aa.tail.head)) {
        return false
    }

    return isSorted(aa.tail, order)
}

class Exercise2 : WordSpec({

    "isSorted" should {
        """with empty list""" {
            isSorted<Int>(
                listOf()
            ) { a, b -> b > a } shouldBe true
        }
        """with list has signle item""" {
            isSorted<Int>(
                listOf(1)
            ) { a, b -> b > a } shouldBe true
        }
        """detect ordering of a list of incorrectly ordered Ints
            based on an ordering HOF""" {
            isSorted(
                listOf(1, 3, 2)
            ) { a, b -> b > a } shouldBe false
        }
        """verify ordering of a list of correctly ordered Strings
            based on an ordering HOF""" {
            isSorted(
                listOf("a", "b", "c")
            ) { a, b -> b > a } shouldBe true
        }
        """verify ordering of a list of incorrectly ordered Strings
            based on an ordering HOF""" {
            isSorted(
                listOf("a", "z", "w")
            ) { a, b -> b > a } shouldBe false
        }
        "return true for an empty list" {
            isSorted(listOf<Int>()) { a, b ->
                b > a
            } shouldBe true
        }
    }
})

// 연습문제 2.3
class Exercise3 : WordSpec({
    fun <A, B, C> curry(f: (A, B) -> C): (A) -> (B) -> C = {
            a:A -> { b:B -> f(a, b) }
    }

    "curry" should {
        """break down a function that takes multiple arguments into
            a series of functions that each take only oneargument""" {

            val f: (Int) -> (Int) -> String =
                curry { a: Int, b: Int -> "$a:$b" }
            val y = f(1)(2)
            val z = f(1)(3)
            y shouldBe "1:2"
            z shouldBe "1:3"
        }
    }
})

// 연습문제 2.4
class Exercise4 : WordSpec({
    fun <A, B, C> uncurry(f: (A) -> (B) -> C): (A, B) -> C = {
            a:A, b:B -> f(a)(b)
    }

    "uncurry" should {
        """take a function accepting two values and then apply that
            function to the components of the pair which is the
            second argument""" {

            val f: (Int, Int) -> String =
                uncurry<Int, Int, String> { a -> { b -> "$a:$b" } }
            f(1, 2) shouldBe "1:2"
            f(1, 3) shouldBe "1:3"
        }
    }
})

// 연습문제 2.5
class Exercise5 : WordSpec({
    fun <A, B, C> compose(f: (B) -> C, g: (A) -> B): (A) -> C = {
            a:A -> f(g(a))
    }

    "compose" should {
        "apply function composition over two functions" {
            val fahrenheit2celsius: (Double) -> String =
                compose<Double, Double, String>(
                    { b -> "$b degrees celsius" },
                    { a -> (a - 32.0) * (5.0 / 9.0) }
                )

            fahrenheit2celsius(68.0) shouldBe "20.0 degrees celsius"
        }
    }
})
