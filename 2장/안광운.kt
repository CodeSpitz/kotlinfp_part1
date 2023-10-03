import kotlin.math.pow

fun main() {
    q2_1()
    q2_2()
    q2_3()
    q2_4()
    q2_5()
}

fun q2_1() {
    println("2.1 - fib(10) : ${fib(10)}")
    println("2.1 - fib(78) : ${fib(78)}")
    println("2.1 - fib(79) : ${fib(79)}")
    println("-----------------------------------------")
}

fun q2_2() {
    val listOf1 = listOf(4, 3, 5, 7, 10, 11)
    val listOf2 = listOf(1, 2, 3, 4, 5, 6)
    val listOf3 = listOf("A", "C", "E", "B", "F")
    val listOf4 = listOf("A", "B", "C", "D", "E")
    val listOf5 = listOf(10, 9, 8, 7, 6, 4)
    val listOf6 = listOf("F", "E", "D", "C", "A")

    println("2.2 - $listOf1 is sorted : ${isSorted(listOf1) { x: Int, y: Int -> x < y }}")
    println("2.2 - $listOf2 is sorted : ${isSorted(listOf2) { x: Int, y: Int -> x < y }}")
    println("2.2 - $listOf3 is sorted : ${isSorted(listOf3) { x: String, y: String -> x < y }}")
    println("2.2 - $listOf4 is sorted : ${isSorted(listOf4) { x: String, y: String -> x < y }}")
    println("2.2 - $listOf5 is reverse order sorted : ${isSorted(listOf5) { x: Int, y: Int -> x > y }}")
    println("2.2 - $listOf6 is reverse order sorted : ${isSorted(listOf6) { x: String, y: String -> x > y }}")
    println("-----------------------------------------")
}

fun q2_3() {
    val rF1 = curry { a: Int, b: Int -> a + b } (1)(2)
    val rF2 = curry { a: String, b: String -> a.plus(b) } ("Functional")("Programing")

    println("2.3 - curry { a: Int, b: Int -> a + b } (1)(2) : $rF1")
    println("2.3 - curry { a: String, b: String -> a.plus(b) } (\"Functional\")(\"Programing\") : $rF2")
    println("-----------------------------------------")
}

fun q2_4() {
    val rF1 = uncurry { a: Int -> { b: Int -> a + b } } (1, 2)
    val rF2 = uncurry { a: String -> { b: String -> a.plus(b) } } ("Functional", "Programing")

    println("2.4 - uncurry { a: Int -> { b: Int -> { a + b} } } (1, 2) : $rF1")
    println("2.4 - uncurry { a: String -> { b: String -> {a.plus(b)} } } (\"Functional\", \"Programing\") : $rF2")
    println("-----------------------------------------")
}

fun q2_5() {
    val rF1 = compose({ b: Double -> b.pow(2) }, { a: Double -> a.pow(2) }) (2.0)

    println("2.5 - compose({ b: Double -> b.pow(2) }, { a: Double -> a.pow(2) }) (2.0) : $rF1")
}

// 2.1
fun fib(i: Int): Int {
    tailrec fun go(n: Int, x: Int, y: Int): Int =
        if (n == 0) {
            x
        } else if (n == 1) {
            y
        } else {
            go(n - 1, y, x + y)
        }

    return go(i, 0, 1)
}

// 2.2
val <T> List<T>.tail: List<T>
    get() = drop(1)

val <T> List<T>.head: T
    get() = first()

fun <A> isSorted(aa: List<A>, order: (A, A) -> Boolean): Boolean {
    tailrec fun go(x: A, xs: List<A>): Boolean =
        if ( xs.isEmpty() ) true
        else if (!order(x, xs.head)) false
        else go(xs.head, xs.tail)

    return aa.isEmpty() || go(aa.head, aa.tail)
}

// 2.3
fun <A, B, C> curry(f: (A, B) -> C): (A) -> (B) -> C =
    { a: A -> { b: B -> f(a, b) } }

// 2.4
fun <A, B, C> uncurry(f: (A) -> (B) -> C): (A, B) -> C =
    { a: A, b: B ->  f(a)(b)  }

// 2.5
fun <A, B, C> compose(f: (B) -> C, g: (A) -> B): (A) -> C =
    { a: A -> f(g(a)) }