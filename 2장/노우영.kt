fun main(args: Array<String>) {
    //2.1
    (0..10).forEach {
        println(fib(it))
    }

    //2.2
    val sorted = isSorted((0..10).toList()) { i, i2 -> i < i2 }
    val notSorted = isSorted((0..10).toList()) { i, i2 -> i > i2 }
    println("isSorted: $sorted")
    println("isSorted: $notSorted")

    //2.3
    val curried = curry{a:Int,b: Int -> a * b}
    val curried2 = curry { a: Int, b: Int -> a * b }(3)(4)
    println("curried: " + curried(1)(2))
    println("curried2: $curried2")

    //2.4
    val uncurried = uncurry(curried)
    println("uncurried: " + uncurried(6, 7))

    //2.5
    val composed = compose({ a: Int -> a * 2 }, { a: Int -> a * 3 })
    val composed2 = compose({ it + 5 }, { a: Int -> a * 10 })
    println("composed: " + composed(2))
    println("composed2: " + composed2(5))
}

fun fib(i: Int): Int {
    tailrec fun go(i: Int, x: Int, y: Int): Int =
        when (i) {
            0 -> x
            else -> go(i - 1, y, x + y)
        }

    return go(i, 0, 1)
}

fun <A> isSorted(aa: List<A>, order: (A, A) -> Boolean): Boolean {
    tailrec fun go(a: List<A>): Boolean {
        if (a.size <= 1) return true
        if (!order(a.head, a[1])) return false
        return go(a.tail)
    }

    return go(aa)
}

fun <A, B, C> partial1(a: A, f: (A, B) -> C): (B) -> C = { b ->
    f(a, b)
}

fun <A, B, C> curry(f: (A, B) -> C): (A) -> (B) -> C = { a ->
    { b: B -> f(a, b) }
}

fun <A, B, C> curry2(f: (A, B) -> C): (A) -> (B) -> C = { a ->
    partial1(a, f)
}

fun <A, B, C> uncurry(f: (A) -> (B) -> C): (A, B) -> C = { a: A, b: B ->
    f(a)(b)
}

fun <A, B, C> compose(f: (B) -> C, g:(A) -> B): (A) -> C = {
    a -> f(g(a))
}

val <T> List<T>.tail: List<T>
    get() = drop(1)

val <T> List<T>.head: T
    get() = first()
