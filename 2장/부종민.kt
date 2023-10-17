import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

fun fib(i: Int): Int {
    fun go(n: Int): Int {
        if(n<=1) return n
        return go(n-1) + go(n-2)
    }
    return go(i)
}

fun fib2(i: Int): Int {
    return when {
        i <= 1 -> i
        else -> fib2(i - 1) +fib2(i - 2)
    }
}

fun fib3(i: Int): Int = if(i <= 1) {i} else { fib3(i-1) + fib3(i-2) }

fun fibonacci(i: Int): Int {
    tailrec fun go(n: Int, a: Int, b: Int): Int {
        return if(n == 0) a
        else if (n ==1) b
        else go(n-1, b, a + b)
    }
    return go(i, 0, 1)
}


class T1 {
    @Test
    fun a() {
        assertEquals(0, fib(0))
        assertEquals(1, fib(1))
        assertEquals(1, fib(2))
        assertEquals(2, fib(3))
        assertEquals(3, fib(4))
        assertEquals(5, fib(5))
        assertEquals(8, fib(6))
        assertEquals(8, fibonacci(6))
        assertEquals(8, fib2(6))
        assertEquals(8, fib3(6))
    }
}

val <T> List<T>.head: T
    get() = first()

val <T> List<T>.tail: List<T>
    get() = drop(1)

fun <A> isSorted(list: List<A>, order: (A, A) -> Boolean): Boolean {
    if(list.size <= 1) return true

    fun loop(list:List<A>): Boolean  {
        val head = list.head
        val tail = list.tail
        val tailHead = tail.head
        return if(list.size == 2) {
            order(head, tailHead)
        } else {
            order(head, tailHead) && loop(tail)
        }
    }

    return loop(list)
}


class T2 {
    @Test
    fun a() {
        val f = { a: Int, b: Int -> a <= b }
        val r1 = isSorted(listOf(1,2,3), f)
        assertTrue(r1)
        val r2 = isSorted(listOf(2,1,3), f)
        assertFalse(r2)
    }
}


fun <A, B, C> curry(f: (A, B) -> C): (A) -> (B) -> C  = {a -> { f(a, it) } }
fun <A, B, C> uncurry(f: (A) ->(B) -> C): (A, B) -> C = {a, b -> f(a)(b)}
fun <A, B, C> compose(f: (B) -> C, g: (A) -> B): (A) -> C = {f(g(it))}


class T3 {
    @Test
    fun test_curry() {
        val f1 = curry{a:Int, b: Int -> a+b}
        val f2 = f1(1)
        val r = f2(2)
        assertEquals(3, r)
    }

    @Test
    fun test_uncurry() {
        val f = uncurry{a:Int -> {b: Int -> a + b}}
        val r = f(1, 2)
        assertEquals(3, r)
    }

    @Test
    fun test_compose() {

        val f = compose({b: Int -> b*2}, {a: Int -> a+4})
        val r = f(1)
        assertEquals(10, r)
    }
}


