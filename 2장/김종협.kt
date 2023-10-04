/*연습문제 2-1*/
fun fib(i: Int): Int {
    tailrec fun go(count: Int, curr: Int, next: Int): Int =
        if   (count == 0) curr
        else go(count - 1, next, curr + next)
    return go(i, 0, 1)
}

/*연습문제 2-2*/
val <T> List<T>.tail: List<T> { get() = drop(1) }
val <T> List<T>.head: T       { get() = first() }

fun <A> isSorted(aa: List<A>, order: (A, A) -> Boolean): Boolean {
    tailrec fun go(x: A, xs: List<A>): Boolean = {
        if (xs.isEmpty()) true
        else if (!order(x, xs.head)) false
        else go(xs.head, xs.tail)
    }
    return aa.isEmpty() || go(aa.head, aa.tail)
}
/*연습문제 2-3*/
fun <A, B, C> curry(f: (A, B) -> C): (A) -> (B) -> C = {
    return a: A -> { b: B -> f(a, b)}
}
/*연습문제 2-4*/
fun <A, B, C> uncurry(f: (A) -> (B) -> C): (A, B) -> C = {
    return a: A, b: B -> f(a)(b)
}
/*연습문제 2-5*/
fun <A, B, C> compose(f: (B) -> C, g: (A) -> B): (A) -> C = {
    return a: A -> f(g(a))
}