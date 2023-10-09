// 3.1
fun <A> tail(xs: List<A>): List<A> =
    when (xs) {
        is Nil -> throw IllegalStateException() // Nil
        is Cons -> xs.tail
    }

// 3.2
fun <A> setHead(xs: List<A>, x: A): List<A> =
    when (xs) {
        is Nil -> throw IllegalStateException() // Nil
        is Cons -> Cons(x, xs.tail)
    }

// 3.3
fun <A> drop(l: List<A>, n: Int): List<A> {
    if (n == 0) return l

    return when (l) {
        is Nil -> throw IllegalStateException() // Nil
        is Cons -> drop(l.tail, n - 1)
    }
}
