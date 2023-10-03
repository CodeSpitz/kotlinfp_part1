// ex 2.1
//fun fib(i: Int): Int = if (i <= 1) i else fib(i - 1) + fib(i - 2)
//fun fib(i: Int): Int {
//    if (i <= 1) return i
//    var a = 0
//    var b = 1
//    var ret = 0
//    for (i in 2..i) {
//        ret = a + b
//        a = b
//        b = ret
//    }
//    return ret
//}
fun fib(i: Int): Int {
    tailrec fun go(offset: Int, a: Int, b: Int): Int =
        if (offset < i) go(offset + 1, b, a + b) else a + b
    return if (i <= 1) i else go(2, 0, 1)
}
fun exam2_1() {
    logd("fib of %d is %d".format(10000, fib(10000)))
}

// ex2.2
val <T> List<T>.tail: List<T> get() = drop(1)
val <T> List<T>.head: T get() = first()
fun <A> isSorted(aa: List<A>, order: (A, A) -> Boolean): Boolean {
    tailrec fun go(head: A, tail: List<A>): Boolean =
        when {
            tail.isEmpty() -> true
            order(head, tail.head) -> go(tail.head, tail.tail)
            else -> false
        }
    return if (aa.isEmpty()) true else go(aa.head, aa.tail)
}
fun exam2_2() {
    logd("isSorted %s".format(
        isSorted(
            aa = listOf("kang", "park", "kim"),
            order = { a1, a2 -> a1 < a2 }
        ).let {
            if (it) "true" else "false"
        }
    ))
}

// ex2.3
fun <A, B, C> curry(f: (A, B) -> C): (A) -> (B) -> C = { a -> { b -> f(a, b) } }

// ex2.4
fun <A, B, C> uncurry(f: (A) -> (B) -> C): (A, B) -> C = { a, b -> f(a)(b) }

// ex2.5
fun <A, B, C> compose(f: (B) -> C, g: (A) -> B): (A) -> C = { a -> f(g(a)) }