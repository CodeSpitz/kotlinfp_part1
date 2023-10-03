// 연습 문제 결과 확인
fun main(args: Array<String>) {
    println("연습문제 2.1 -> fib(9)의 값은 " + fib(9))
    println("연습문제 2.2 -> isSorted()의 값은 " + isSorted(listOf(1,4,9)) { a1, a2 -> a1 <= a2 })
    println("연습문제 2.2 -> isSorted()의 값은 " + isSorted(listOf(1,4,9)) { a1, a2 -> a1 >= a2 })
}

// 연습문제 2.1
fun fib(i: Int): Int {
    tailrec fun go(n: Int, current: Int, next: Int): Int =
        if (n == 1) current
        else go(n - 1, next, current + next)
    return if (i <= 0) -1 else go(i, 0, 1)
}


// 연습문제 2.2
val <T> List<T>.tail: List<T>
    get() = drop(1)

val <T> List<T>.head: T
    get() = first()

fun <A> isSorted(aa: List<A>, order: (A, A) -> Boolean): Boolean {
    tailrec fun go(prev: A, list: List<A>, f: (A, A) -> Boolean): Boolean =
        if (list.isEmpty()) true
        else if (f(prev, list.head)) go(list.head, list.tail, f)
        else false
    return go(aa.head, aa.tail, order)
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
