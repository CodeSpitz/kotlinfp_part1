// 연습문제 2.1 ----------
fun fib(i: Int): Int {
    require(i >= 1)

    tailrec fun fibonachi(n: Int, before: Int = 0, last: Int = 1): Int {
        return if (n <= 1) {
            last
        } else {
            fibonachi(n - 1, last, last + before)
        }
    }

    return if (i <= 1) {
        0
    } else {
        fibonachi(i - 1)
    }
}

println( fib(1) )
println( fib(2) )
println( fib(5) )
println( fib(9) )


// 연습문제 2.2 ----------
val <T> List<T>.tail: List<T>
    get() = drop(1)

val <T> List<T>.head: T
    get() = first()

fun <A> isSorted(aa: List<A>, order: (A, A) -> Boolean): Boolean {
    if (aa.size < 2) return true

    tailrec fun check(list: List<A>, lastHead: A, order: (A, A) -> Boolean): Boolean {
        val head = list.head
        return when {
            (list.size == 1) -> order(lastHead, head)
            order(lastHead, head) -> check(list.tail, list.head, order)
            else -> false
        }
    }

    return check(aa.tail, aa.head, order)
}

println( isSorted(listOf(1, 3, 2, 4, 5, 6, 7, 8), { left: Int, right: Int -> (left <= right) } ) )
println( isSorted(listOf(1, 2, 3, 4, 5, 6, 7, 8), { left: Int, right: Int -> (left <= right) } ) )
println( isSorted(listOf(1, 1, 2, 2, 3, 4), { left: Int, right: Int -> (left <= right) } ) )
println( isSorted(listOf(8, 7, 6, 5, 3, 4, 2, 1), { left: Int, right: Int -> (left > right) } ) )
println( isSorted(listOf(8, 7, 6, 5, 4, 3, 2, 1), { left: Int, right: Int -> (left > right) } ) )



// 연습문제 2.3 ----------
fun <A, B, C> curry(f: (A, B) -> C): (A) -> (B) -> C = { a: A -> { b: B -> f(a, b) } }

val currySum = curry{ a: Int, b: Int -> (a + b) }
println( currySum(2)(3) )



// 연습문제 2.4 ----------
fun <A, B, C> uncurry(f: (A) -> (B) -> C): (A, B) -> C = { a: A, b: B -> f(a)(b) }

val uncurrySum = uncurry{ a: Int -> { b: Int -> a + b } }
println( uncurrySum(2, 3) )



// 연습문제 2.5 ----------
fun <A, B, C> compose(f: (B) -> C, g: (A) -> B): (A) -> C = { a: A -> f(g(a)) }

val fun1 = { a: Int -> a * 2 }
val fun2 = { a: Int -> a / 2 }
val composeFun = compose(fun1, fun2)
println( composeFun(2) )
