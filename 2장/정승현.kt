assert(fib(0) == 0)
assert(fib(5) == 5)

fun fib(i: Int): Int = when (i) {
    0 -> 0
    1 -> 1
    else -> fib(i - 2) + fib(i - 1)
}

val <T> List<T>.tail: List<T>
    get() = drop(1)

val <T> List<T>.head: T
    get() = first()

fun <A> isSorted(list: List<A>, order: (A, A) -> Boolean): Boolean {
    fun go(a: A, list: List<A>): Boolean = when {
        list.isEmpty() -> true
        !order(a, list.head) -> false
        else -> go(list.head, list.tail)
    }
    return go(list.head, list.tail)
}

val successCase1 = listOf(1, 2, 3, 4, 5)
val failCase1 = listOf(1, 2, 3, 5, 4)

assert(isSorted(successCase1) { a1, a2 -> a1 < a2 })
assert(isSorted(failCase1) { a1, a2 -> a1 < a2 }

fun <A, B, C> curry(f: (A, B) -> C): (A) -> (B) -> (C) = { a: A -> { b: B -> f(a,b) } }
fun <A, B, C> uncurry(f: (B) -> C, g: (A) -> B): (A) -> C = { a: A -> f(g(a)) }
fun <A, B, C> compose(f: (B) -> C, g: (A) -> B): (A) -> C = { a: A -> f(g(a)) }

