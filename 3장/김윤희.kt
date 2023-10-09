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
