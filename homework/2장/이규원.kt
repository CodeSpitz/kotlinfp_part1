fun main() {
    // 2.1
    println("2.1 " + (fib(1) == 1))
    println("2.1 " + (fib(2) == 1))
    println("2.1 " + (fib(3) == 2))
    println("2.1 " + (fib(6) == 8))
    println("2.1 " + (fib(8) == 21))
    println("2.1 " + (fib(11) == 89))

    // 2.2
    println("2.2 " + (isSorted(listOf(1, 2, 3, 4, 5, 5)) { e1, e2 -> e1 <= e2 }))
    println("2.2 " + (isSorted(listOf('A', 'B', 'B', 'C')) { e1, e2 -> e1 <= e2 }))
    println("2.2 " + (!isSorted(listOf("A", "B", "D", "C")) { e1, e2 -> e1 <= e2 }))

}

// 2.1
fun fib(i: Int): Int {
    tailrec fun go(n: Int, a: Int, b: Int): Int {
        return if (n == 0) a
        else go(n - 1, b, a + b)
    }
    return go(i, 0, 1)
}

// 2.2
val <T> List<T>.tail: List<T>
    get() = drop(1)

val <T> List<T>.head: T
    get() = first()

fun <A> isSorted(list: List<A>, order: (A, A) -> Boolean): Boolean {
    tailrec fun go(n: A, l: List<A>): Boolean =
        if (l.isEmpty()) true
        else if (!order(n, l.head)) false
        else go(l.head, l.tail)

    return list.isEmpty() || go(list.head, list.tail)
}

// 2.3
fun <A, B, C> curry(f: (A, B) -> C): (A) -> (B) -> C = { a: A -> { b: B -> f(a, b) } }

// 2.4
fun <A, B, C> uncurry(f: (A) -> (B) -> (C)): (A, B) -> C = { a: A, b: B -> f(a)(b) }

// 2.5
fun <A, B, C> compose(f: (B) -> C, g: (A) -> B): (A) -> C = { a: A -> f(g(a)) }
