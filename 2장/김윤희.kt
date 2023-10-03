// 2.1
fun fib(i: Int): Int {
    tailrec fun go(i: Int, current: Int, next: Int): Int =
        if (i <= 0)
            current
        else go(i - 1, next, current + next) + 1
    return go(i, 0, 1)
}

// 2.2
fun <A> isSorted(aa: List<A>, order: (A, A) -> Boolean): Boolean {
    tailrec fun go(a: A, restList: List<A>): Boolean =
        if (restList.isEmpty()) true
        else if (!order(a, restList.head)) false
        else go(restList.head, restList.tail)
    return aa.isEmpty() || go(aa.head, aa.tail)
}

// 2.3
fun <A, B, C> curry(f: (A, B) -> C): (A) -> (B) -> C =
    { a: A -> { b: B -> f(a, b) } }

// 2.4
fun <A, B, C> uncurry(f: (A) -> (B) -> C): (A, B) -> C =
    { a: A, b: B -> f(a)(b) }

// 2.5
fun <A, B, C> compose(f: (B) -> C, g: (A) -> B): (A) -> C =
    { a: A -> f(g(a)) }
