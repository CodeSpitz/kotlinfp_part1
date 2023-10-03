import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class Exercises {
    /**
     * 연습문제 2.1 n번째 피보나치 수를 구하는 함수
     * - 지역적인 꼬리 재귀 함수를 사용
     */
    fun fib(i: Int): Int {
        tailrec fun go(n: Int, a: Int, b: Int): Int {
            return when (n) {
                0 -> a
                1 -> b
                else -> go(n - 1, b, a + b)
            }
        }
        return go(i, 0, 1)
    }

    @Test
    fun fibTest() {
        val expectedResults = listOf(0, 1, 1, 2, 3, 5, 8, 13, 21, 34, 55)
        for (i in expectedResults.indices) {
            val result = fib(i)
            assertEquals(expectedResults[i], result)
        }
    }

    /**
     * 연습문제 2.2 List<A> 타입의 단일 연결리스트가 비교 함수에 맞춰 적절히 정렬되어 있는지 검사하는 함수
     * - tail과 head 확장 프로퍼티 (Extension property) 활용
     */
    fun <A> isSorted(aa: List<A>, order: (A, A) -> Boolean): Boolean {
        tailrec fun go(l: List<A>): Boolean {
            return when {
                l.size <= 1 -> true
                !order(l.head, l.tail.head) -> false
                else -> go(l.tail)
            }
        }
        return go(aa)
    }

    @Test
    fun isSortedTest() {
        assertTrue(isSorted(listOf(1, 2, 3, 4, 5)) { a, b -> a <= b })
        assertFalse(isSorted(listOf(1, 2, 3, 5, 4)) { a, b -> a <= b })
        assertTrue(isSorted(listOf("a", "b", "c", "d", "e")) { a, b -> a <= b })
        assertTrue(isSorted(listOf("a", "aa", "aaa", "aaa", "aaaa")) { a, b -> a.length <= b.length })
        assertFalse(isSorted(listOf("a", "aa", "aaa", "aaa", "aaaa")) { a, b -> a.length < b.length })
        assertTrue(isSorted(listOf(true, true, false, false)) { a, b -> a >= b })
        assertFalse(isSorted(listOf(false, false, true, true)) { a, b -> a > b })
    }

    /**
     * 연습문제 2.3 Currying (인자를 두 개 받는 함수 f를 받아서 첫 번째 인자를 f에 부분 적용한 새 함수) 구현
     */
    fun <A, B, C> curry(f: (A, B) -> C): (A) -> (B) -> C = { a -> { b -> f(a, b) } }

    @Test
    fun testCurry() {
        fun add(a: Int, b: Int): Int = a + b
        val add2 = curry(::add)(2)
        assertEquals(add2(3), 5)
        assertEquals(add2(4), 6)
        assertEquals(add2(5), 7)
    }

    /**
     * 연습문제 2.4 curry의 역변환인 uncurry 구현
     */
    fun <A, B, C> uncurry(f: (A) -> (B) -> C): (A, B) -> C = { a, b -> f(a)(b) }

    @Test
    fun testUncurry() {
        fun add(a: Int, b: Int): Int = a + b
        val curriedAdd = curry(::add)
        assertEquals(curriedAdd(2)(3), 5)
        val uncurryiedAdd = uncurry(curriedAdd)
        assertEquals(uncurryiedAdd(3, 3), 6)
    }

    /**
     * 연습문제 2.5 두 함수를 합성하는 고차 함수
     */
    fun <A, B, C> compose(f: (B) -> C, g: (A) -> B): (A) -> C = { a -> f(g(a)) }

    @Test
    fun testCompose() {
        fun abs(n: Int): Int = if (n < 0) -n else n
        fun changeStatusFormat(v: Int): String = if (v > 0) "$v% 변동" else "변동 없음"

        val absAndChangeStatusFormat = compose(::changeStatusFormat, ::abs)
        assertEquals(absAndChangeStatusFormat(10), "10% 변동")
        assertEquals(absAndChangeStatusFormat(-10), "10% 변동")
        assertEquals(absAndChangeStatusFormat(0), "변동 없음")
    }
}