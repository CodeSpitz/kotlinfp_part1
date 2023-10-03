//연습문제 2.1
class Chapter2 {
    fun fib(i: Int): Int {
        tailrec fun go(n: Int, currnet: Int, next: Int): Int =
                if (n < 0) 0;
                else if (n == 0) currnet;
                else go(n - 1, next, currnet + next);
        return go(i, 0, 1);
    }

    //연습문제 2.2
    fun <A> isSorted(aa: List<A>, order: (A, A) -> Boolean): Boolean {
        tailrec fun go(aa: List<A>): Boolean =
                if (aa.isEmpty() || aa.size == 1) true;
                else if (!order(aa.head, aa.tail.head)) false;
                else go(aa.tail);
        return go(aa);
    }

    //연습문제 2.3
    fun <A, B, C> curry(f: (A, B) -> C): (A) -> (B) -> C = { a: A -> { b: B -> f(a, b) } }

    //연습문제 2.4
    fun <A, B, C> uncurry(f: (A) -> (B) -> C): (A, B) -> C = { a: A, b: B -> f(a)(b) }

    //연습문제 2.5
    fun <A, B, C> compose(f: (B) -> C, g: (A) -> B): (A) -> C = { a: A -> f(g(a)) }

    val <T> List<T>.tail: List<T>
        get() = drop(1)

    val <T> List<T>.head: T
        get() = first()
}
