// 연습문제 2.1
tailrec fun _fib(curr: Int, target: Int, pprev: Int, prev: Int): Int = 
  if(curr == target) pprev + prev
  else _fib(curr + 1, target, prev, pprev + prev)

fun fib(i: Int): Int = when(i) {
    in 0..1 -> i
    else -> _fib(2, i, 0, 1)
}

// 연습문제 2.2
val <T> List<T>.tail: List<T>
  get() = drop(1)

val <T> List<T>.head: T
  get() = first()

fun <ITEM> isSorted(aa: List<ITEM>, order: (ITEM, ITEM) -> Boolean): Boolean {
    tailrec fun go(head: ITEM, tail: List<ITEM>): Boolean =
        when {
            tail.isEmpty() -> true
            order(head, tail.head) -> go(tail.head, tail.tail)
            else -> false
        }
    return if (aa.isEmpty()) true else go(aa.head, aa.tail)
}

// 연습문제 2.3
fun <A, B, C> curry(f: (A, B) -> C): (A) -> (B) -> C = { a -> { b -> f(a, b) } }

// 연습문제 2.4
fun <A, B, C> uncurry(f: (A) -> (B) -> C): (A, B) -> C = { a, b -> f(a)(b) }

// 연습문제 2.5
fun <A, B, C> compose(f: (B) -> C, g: (A) -> B): (A) -> C = { a -> f(g(a)) }
