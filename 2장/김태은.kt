// 2.1
fun fib(i: Int): Int {
    fun loop(i: Int): Int =
        if (i == 0) 0
        else if (i == 1) 1
        else loop(i - 1) + loop(i - 2)

    return loop(i)
}

fun fib2(i: Int): Int {
    tailrec fun loop(i: Int, current: Int, next: Int): Int =
        if (i == 0) current
        else loop(i - 1, next, current + next)

    return loop(i, 0, 1)
}

// 2.2
fun <A> isSorted(aa: List<A>, order: (A, A) -> Boolean): Boolean {
    tailrec fun loop(a: A, aa: List<A>): Boolean =
        if (aa.isEmpty()) true
        else if (!order(a, aa.head)) false
        else loop(aa.head, aa.tail)

    return aa.isEmpty() || loop(aa.head, aa.tail)
}

// 2.3
fun <A, B, C> curry(f: (A, B) -> C): (A) -> (B) -> C =
    { a: A ->
        { b: B ->
            f(a, b)
        }
    }

// 2.4
fun <A, B, C> uncurry(f: (A) -> (B) -> C): (A, B) -> C =
    { a: A, b: B -> f(a)(b) }

// 2.5
fun <A, B, C> compose(f: (B) -> C, g: (A) -> B): (A) -> C =
    { a: A -> f(g(a)) }
