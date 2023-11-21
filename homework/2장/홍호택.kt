package repo.lec2

fun main() {
    val n = 20 // 원하는 피보나치 수열의 항 번호
    println("연습문제 2.1: 피보나치 수열의 $n 번째 항? ${fib(n)}")

    val shuffledList = (1..100).shuffled()
    println("연습문제 2.2: 정렬된 리스트인가? ${isSorted(shuffledList) { a, b -> a <= b }}")

    val add= { a:Int, b:Int -> a + b }
    println("연습문제 2.3: add 함수를 사용한 curry에 대해서 1, 2의 결과 값? ${curry(add)(1)(2)}")

    val curriedAdd = { a: Int -> { b: Int -> add(a, b) } }
    println("연습문제 2.4: add 함수를 사용한 uncurry에 대해서 1, 2의 결과 값? ${uncurry(curriedAdd)(1,2)}")

    val square: (Int) -> Int = { x -> x * x }
    val double: (Int) -> Int = { x -> x * 2 }
    println("연습문제 2.5: 2 인자에 대한 두 함수 compose 결과? ${compose(square, double)(2)}")

}

// ==============================
// 연습문제 2.1
// ==============================
fun fib(n: Int): Int {
    tailrec fun go(n: Int, a: Int = 0, b: Int =1): Int {
        return when (n) {
            0 -> a
            1 -> b
            else -> go(n - 1, b, a + b)
        }
    }

    // 시작 값 a와 b를 0과 1로 초기화하고 피보나치 수열의 n번째 항을 계산합니다.
    return go(n)
}


// ==============================
// 연습문제 2.2
// ==============================
val <T> List<T>.tail: List<T>
    get() = drop(1)

val <T> List<T>.head: T
    get() = first()


fun <A> isSorted(aa: List<A>, order: (A, A) -> Boolean): Boolean {
    tailrec fun go(list: List<A>): Boolean {
        if (list.size < 2) {
            return true
        }
        val first = list.head
        val second = list.tail.head
        if (!order(first, second)) {
            return false
        }
        return go(list.tail)
    }

    return go(aa)
}


// ==============================
// 연습문제 2.3
// ==============================
fun <A, B, C> curry(f: (A, B) -> C): (A) -> (B) -> C {
    return { a: A ->
        { b: B ->
            f(a, b)
        }
    }
}

// ==============================
// 연습문제 2.4
// ==============================
fun <A, B, C> uncurry(f: (A) -> (B) -> C): (A, B) -> C =
    { a, b -> f(a)(b) }

// ==============================
// 연습문제 2.5
// ==============================
fun <A, B, C> compose(f: (B) -> C, g: (A) -> B): (A) -> C {
    return { x: A -> f(g(x)) }
}