// 연습문제 2.1
fun fib(i: Int): Int {
    tailrec fun go(count: Int, current: Int, next: Int): Int =
            if(count == 0)
                current
            else go(count - 1, next, current + next)
    return go(i, 0, 1)
}

// 연습문제 2.2
// 첫번째 원소를 제외한 나머지 원소 반환
val <T> List<T>.tail: List<T>
    get() = drop(1)

// 첫번째 원소 반환
val <T> List<T>.head: T
    get() = first()

fun <A> isSorted(aa: List<A>, order: (A, A) -> Boolean): Boolean {
    tailrec fun go(x: A, xs: List<A>): Boolean =
            if (xs.isEmpty()) true
            else if (!order(x, xs.head)) false
            else go(xs.head, xs.tail)
    return aa.isEmpty() || go(aa.head, aa.tail)
}

// 연습문제 2.3
fun <A, B, C> curry(f: (A, B) -> C): (A) -> (B) -> C = {
    a: A -> { b: B -> f(a, b) }
}

// 연습문제 2.4
fun <A, B, C> uncurry(f: (A) -> (B) -> C): (A, B) -> C = {
    a: A, b: B -> f(a)(b)
}

// 연습문제 2.5
fun <A, B, C> compose(f: (B) -> C, g: (A) -> B): (A) -> C = {
    a: A -> f(g(a))
}

fun main() {
    println("fib(10) = ${fib(10)}")
    println("isSorted(listOf(1, 2, 3, 4, 5, 6), { a: Int, b: Int -> b > a }) = ${isSorted(listOf(1, 2, 3, 4, 5, 6), { a: Int, b: Int -> b > a })}")
    println("curry({a, b -> a + b})(1)(2) = ${curry({ a: Int, b: Int -> a + b })(1)(2)}")
    println("uncurry { a: Int-> { b: Int-> a+b } }(1, 2) = ${uncurry { a: Int -> { b: Int -> a + b } }(1, 2)}")
    println("compose(fun (b: Int)= b+1, fun (a: Int)= a+1)(1) = ${compose(fun(b: Int) = b + 1, fun(a: Int) = a + 1)(1)}")
}