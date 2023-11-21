// example 2.1
fun fib (i: Int): Int {
    tailrec fun go(n: Int, a: Int, b: Int): Int =
        when (n) {
            1 -> 0
            2 -> b
            else -> go(n - 1, b, a + b)
        }
    return go(i, 0, 1)
}

// example 2.2
fun <A> isSorted(aa: List<A>, order: (A, A) -> Boolean): Boolean {
    tailrec fun loop(a: A, bb: List<A>): Boolean =
        if (bb.isEmpty()) true
        else {
            if (order(a, bb.head)) loop(bb.head, bb.tail) else false
        }
    return loop(aa.head, aa.tail)
}

// example 2.3
fun <A, B, C> curry(f: (A, B) -> C): (A) -> (B) -> C =
    { a: A -> { b: B -> f(a, b) } }

// example 2.4
fun <A, B, C> uncurry(f: (A) -> (B) -> C): (A, B) -> C =
    { a, b -> f(a)(b) }

// example 2.5
fun <A, B, C> compose(f: (B) -> C, g: (A) -> B): (A) -> C =
    { a -> f(g(a))}
