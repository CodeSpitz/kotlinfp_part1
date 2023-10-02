// 2.1
fun fib(i: Int): Int {
  tailrec fun go(n: Int, a: Int, b: Int): Int =
      if (n == 0) a else go(n - 1, b, a + b)

  return go(i, 0, 1)
}

// 2.2
val <T> List<T>.tail: List<T>
    get() = drop(1)

val <T> List<T>.head: T
    get() = first()

tailrec fun <A> isSorted(list: List<A>, order: (A, A) -> Boolean): Boolean {
    if (list.tail.isEmpty()) {
        return true
    }

    if (!order(list.head, list.tail.head)) {
        return false
    }

    return isSorted(list.tail, order)
}

// 2.3
fun <A, B, C> curry(f: (A, B) -> C): (A) -> (B) -> C = { a: A ->
  { b: B -> f(a, b) }
}

// 2.4
fun <A, B, C> uncurry(f: (A) -> (B) -> C): (A, B) -> C = { a: A, b: B ->
  f(a)(b)
}

// 2.5
fun <A, B, C> compose(f: (B) -> C, g: (A) -> B): (A) -> C = { a: A ->
  f(g(a))
}
