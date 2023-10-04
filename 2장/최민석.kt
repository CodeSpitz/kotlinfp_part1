
/**
 *  ```
@Test
fun fibTest() {
    assert(fib(0) == 0)
    assert(fib(1) == 1)
    assert(fib(2) == 1)
    assert(fib(3) == 2)
    assert(fib(4) == 3)
    assert(fib(5) == 5)
    assert(fib(6) == 8)
    assert(fib(7) == 13)
}
 ```
 */
fun fib(i: Int): Int {
    tailrec fun go(curIdx: Int, targetIdx: Int, prev: Int, acc: Int): Int {
        return when (targetIdx) {
            0 -> 0
            1 -> 1
            curIdx -> acc
            else -> go(curIdx+1, targetIdx, acc, acc + prev)
        }
    }

    return go(1 , i, 0, 1)
}


val <T> List<T>.tail: List<T> get() = drop(1)
val <T> List<T>.head: T get() = first()

/**
 ```
 @Test
 fun isSortedTest() {
    assert(isSorted(listOf(1,2,3,4)) { a, b -> a < b })
    assert(!isSorted(listOf(1,3,2,4)) { a, b -> a < b })
 }
 ```
 */
fun <A> isSorted(aa: List<A>, order: (A, A) -> Boolean): Boolean {
    return when {
        aa.count() <= 1 -> true
        !order(aa.head, aa.tail.head) -> false
        else -> isSorted(aa.tail, order)
    }
}


fun <A, B, C> curry(f: (A, B) -> C): (A) -> (B) -> C = { a ->
    { b ->
        f(a, b)
    }
}

fun <A, B, C> uncurry(f: (A) -> (B) -> C): (A, B) -> C = { a, b -> f(a)(b) }

fun <A, B, C> compose(f: (B) -> C, g: (A) -> B): (A) -> C = { a -> f(g(a)) }

