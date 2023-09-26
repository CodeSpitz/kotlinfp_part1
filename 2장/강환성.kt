package codespitz_study_kotlinfunctionalprogramming.`2장`

// 연습문제 2.1
fun fib(i: Int): Int {
    tailrec fun go(n: Int, a: Int= 0, b: Int= 1): Int {
        return if (n == 0) a else go(n - 1, b, a + b)
    }

    return go(i)
}

// 연습문제 2.2
val <T> List<T>.tail: List<T> get() = drop(1)
val <T> List<T>.head: T get() = first()
fun <A> isSorted(aa: List<A>, order: (A, A)-> Boolean): Boolean {
    fun go(x: A, xs: List<A>): Boolean =
        when {
            xs.isEmpty()-> true
            !order(x, xs.head)-> false
            else-> go(xs.head, xs.tail)
        }

    return aa.isEmpty() || go(aa.head, aa.tail);
}

// 연습문제 2.3
fun <A, B, C> curry(f: (A, B)-> C): (A)-> (B)-> C= {
    a-> { b-> f(a, b) }
}

// 연습문제 2.4
fun <A, B, C> uncurry(f: (A)-> (B)-> C): (A, B)-> C= {
    a, b-> f(a)(b)
}

// 연습문제 2.5
fun <A, B, C> compose(f: (B)-> C, g: (A)-> B): (A)-> C= {
    a-> f(g(a))
}

fun main() {
    println("2. 코틀린으로 함수형 프로그래밍 시작하기")
    println("2.1: "+ listOf(0, 1, 2, 3, 4, 5, 6, 7, 8).map { fib(it) })

    println("2.2: "+ isSorted(listOf(1, 2, 3)) { a, b -> a <= b })
    println("2.2: "+ isSorted(listOf("1", "3", "2")) { a, b -> a <= b })

    println("2.3: "+ curry(fun (a: Int, b: Int)= a+b)(1)(2))
    println("2.3: "+ curry { a: Int, b: Int -> a + b }(1)(2))

    println("2.4: "+ uncurry(fun (a: Int)= fun (b: Int)= a+b)(1, 2))
    println("2.4: "+ uncurry { a: Int-> { b: Int-> a+b } }(1, 2))

    println("2.5: "+ compose(fun (b: Int)= b+1, fun (a: Int)= a+1)(1))
    println("2.5: "+ compose({ b: Int-> b+1 }) { a: Int-> a + 1 }(1))
}
