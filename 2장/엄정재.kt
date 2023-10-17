// 연습문제 2.1
fun fib(i: Int): Int = fibonacciTailRec(i, 0, 1)

tailrec fun fibonacciTailRec(n: Int, a: Int, b: Int): Int {
    return if (n == 0) a
    else fibonacciTailRec(n - 1, b, a + b)
}

// 연습문제 2.2
val <T> List<T>.tail: List<T>
    get() = drop(1)

val <T> List<T>.head: T
    get() = first()

fun <A> isSorted(aa: List<A>, order: (A, A) -> Boolean): Boolean {
    if (aa.size <= 1) return true
    return order(aa.head, aa.tail.head) && isSorted(aa.tail, order)
}

// 연습문제 2.3
fun <A, B, C> curry(f: (A, B) -> C): (A) -> (B) -> C {
    return { a: A -> { b: B -> f(a, b) } }
}

// 연습문제 2.4
fun <A, B, C> uncurry(f: (A) -> (B) -> C): (A, B) -> C {
    return { a: A, b: B -> f(a)(b) }
}

// 연습문제 2.5
fun <A, B, C> compose(f: (B) -> C, g: (A) -> B): (A) -> C {
    return { a: A -> f(g(a)) }
}