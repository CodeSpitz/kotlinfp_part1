package com.sfn.cancun

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*


class `2강` {
    @Test
    fun `fib test`() {
        assertEquals(fib(49), 12586269025)
    }

    private fun fib(i: Int): Long {
        tailrec fun solutionHere(n: Int, first: Long, second: Long): Long =
            if (n < 1)
                second
            else
                solutionHere(n - 1, second, first + second)

        return solutionHere(i, 0L, 1L)
    }

    @Test
    fun `isSorted test`() {
        assertTrue(isSorted(listOf(1, 2, 3, 4, 5)) { a, b -> a <= b })
        assertFalse(isSorted(listOf(1, 2, 7, 4, 5)) { a, b -> a <= b })
    }

    private val <T> List<T>.tail: List<T>
        get() = drop(1)

    private val <T> List<T>.head: T
        get() = first()

    private fun <A> isSorted(aa: List<A>, order: (A, A) -> Boolean): Boolean =
        when {
            aa.size <= 1 -> true
            !order(aa.head, aa.tail.head) -> false
            else -> isSorted(aa.tail, order)
        }

    @Test
    fun `curryUncurryTest`() {
        val plus = curry { a: Int, b: Int -> a + b }
        val curriedByPlus1 = plus(1)
        assertEquals(3, curriedByPlus1(2))
        assertEquals(100, curriedByPlus1(99))

        val uncurry = uncurry(plus)
        assertEquals(3, uncurry(1, 2))
        assertEquals(100, uncurry(1, 99))
    }

    private fun <A, B, C> curry(f: (A, B) -> C): (A) -> (B) -> C =
        { a: A -> { b: B -> f(a, b) } }

    private fun <A, B, C> uncurry(f: (A) -> (B) -> C): (A, B) -> C =
        { a: A, b: B -> f(a)(b) }

    @Test
    fun `composeTest`() {
        val addLamda = { i: Int -> i + 10 }
        val addTest = compose(addLamda, addLamda)

        assertEquals(21, addTest(1))
        assertEquals(23, addTest(3))
    }

    private fun <A, B, C> compose(f: (B) -> C, g: (A) -> B): (A) -> C =
        { a: A -> f(g(a)) }
}