package playground

//2.1
fun fib(i: Int): Int {
    tailrec fun recur(ii: Int, curr: Int, next: Int): Int =
        if   (ii == 0) curr
        else recur(ii - 1, next, curr + next)
    return recur(i, 0, 1)
}


//2.1
val <T> List<T>.tail: List<T>
    get() = drop(1)
val <T> List<T>.head: T
    get() = first()
fun <A> isSorted(aa: List<A>, order: (A, A) -> Boolean): Boolean {
    tailrec fun recur(aa: List<A>): Boolean =
            if (aa.isEmpty() || aa.size == 1) true;
            else if (!order(aa.head, aa.tail.head)) false;
            else recur(aa.tail);
    return recur(aa);
}

//2.3
fun <A, B, C> curry(f: (A, B) -> C): (A) -> (B) -> C = {
    return a: A -> { b: B -> f(a, b)}
}

//2.4
fun <A, B, C> uncurry(f: (A) -> (B) -> C): (A, B) -> C = {
    return a: A, b: B -> f(a)(b)
}

//2.5
fun <A, B, C> compose(f: (B) -> C, g: (A) -> B): (A) -> C = {
    return a: A -> f(g(a))
}
