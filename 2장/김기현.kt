// 2.1
fun fib(i: Int): Int {
    tailrec fun go(n: Int, prev: Int, acc: Int): Int =
        if (n == 0) prev else go(n - 1, acc, prev + acc)

    return go(i, 0, 1)
}

// 2.2
fun <A> isSorted(aa: List<A>, order: (A, A) -> Boolean): Boolean {
    tailrec fun go(list: List<A>): Boolean {
        if (list.size <= 1) {
            return true
        }
        if (!order(list.head, list[1])) {
            return false
        }
        return go(list.tail)
    }

    return go(aa)
}

// 2.3
fun <A, B, C> curry(f: (A, B) -> C): (A) -> (B) -> C = { a: A -> { b: B -> f(a, b)}}

// 2.4
fun <A, B, C> uncurry(f: (A) -> (B) -> C): (A, B) -> C = {a: A, b: B -> f(a)(b)}

// 2.5
fun <A, B, C> compose(f: (B) -> C, g: (A) -> B): (A) -> C = {a: A -> f(g(a))}
