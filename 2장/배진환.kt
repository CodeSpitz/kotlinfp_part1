
// 2.1
fun fib(n: Int): Int {
    fun fN(v0: Int , v1: Int) = v0 + v1
    tailrec fun loop(v0: Int, v1: Int, from: Int = 2, to: Int = n): Int =
        if (from == to) fN(v0, v1) else loop(v1, fN(v0, v1), from + 1, to)

    return (0 to 1).let {
        (v0, v1) -> when {
            n <= 0 -> v0
            n == 1 -> v1
            else -> loop(v0, v1)
        }
    }
}

// 2.3
val <T> List<T>.tail: List<T>
    get() = drop(1)

val <T> List<T>.head: T
    get() = first()

tailrec fun <T> isSorted(list: List<T>, order: (T, T) -> Boolean): Boolean {
    val rest = list.tail

    return when {
        rest.isEmpty() -> true
        !order(list.head, rest.head) -> false
        else -> isSorted(rest, order)
    }
}

// 2.3
fun <A, B, C> curry(f: (A, B) -> C): (A) -> (B) -> C = { a: A ->
    { b: B -> f(a, b) }
}

// 2.4
fun <A, B, C> uncurry(f: (A) -> (B) -> C): (A, B) -> C = { a: A, b: B ->
    f(a)(b)
}

// 2.5
fun <A, B, C> compose(f: (B) -> C, g: (A) -> B): (A) -> C = { a: A ->
    f(g(a))
}
