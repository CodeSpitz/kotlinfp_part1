
// 2-1 피보나치 수 계산 fib(0) = 0, fib(1) = 1, fib(2) = fib(0) + fib(1), .... fib(n) = fib(n-2) + fib(n-1)
fun fib(num: Int): Int {
    tailrec fun go(n: Int, a: Int, b: Int): Int =
        if (n == 0) a
        else go(n - 1, b, a + b)

    return go(num, 0, 1)
}

// 2-2 List<A> 타입을 술어 함수에 맞춰 적절히 정렬돼 있는지 검사.
val <T> List<T>.tail: List<T>
    get() = drop(1)  // drop(n) : 앞에서 n개의 요소를 제외한 새로운 리스트를 반환, 리스트의 갯수가 작으면 빈 리스트 반환

val <T> List<T>.head: T
    get() = first()

tailrec fun <A> isSorted(list: List<A>, order: (A, A) -> Boolean): Boolean =
    if (list.tail.isEmpty()) true
    else if (!order(list.head, list.tail.head)) false
    else isSorted(list.tail, order)

// 2-3 함수 커링
fun <A, B, C> curry(f: (A, B) -> C): (A) -> (B) -> C = { a: A -> { b: B -> f(a, b) } }

// 2-4 커링 해제
fun <A, B, C> uncurry(f: (A) -> (B) -> C): (A, B) -> C = { a: A, b: B -> f(a)(b) }

// 2-5 함수 합성
fun <A, B, C> compose(f: (B) -> C, g: (A) -> B): (A) -> C = { a: A -> f(g(a)) }

fun main() {
    println("--fib")
    println(fib(0))
    println(fib(1))
    println(fib(2))
    println(fib(3))
    println(fib(4))
    println(fib(5))
    println(fib(6))

    println("--isSorted")
    println(isSorted(listOf(1, 2, 3)) { a, b -> a <= b })
    println(isSorted(listOf(1, 3, 2)) { a, b -> a <= b })
    println(isSorted(listOf<Int>()) { a, b -> a <= b })
    println(isSorted(listOf<Int>(3)) { a, b -> a <= b })

    println("--curry")
    println(curry { a: Double, b: Int -> "String - $a$b" }(11.23)(2))

    println("--uncurry")
    println(uncurry{ a: Int -> { b: Double -> "String - $a$b" } }(1, 22.34))

    println("--compose")
    println( compose( {b: Int -> "String - $b" }, { a: Double -> a.toInt() + 1})(12.23))
}