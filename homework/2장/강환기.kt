import java.util.Scanner
import kotlin.IllegalArgumentException

/*  연습문제 2.1
    n번째 피보나치 수를 얻기 위한 재귀 함수를 작성하라. 처음 두 피보나치 수는 0과 1이다.
    n번째 피보나치 수는 자신 바로 앞의 두 피보나치 수의 합이다.
    피보나치 수열은 0, 1, 1, 2, 3, 5, 8, 13, 21 등 이다.
    여러분의 함수 정의는 지역적인 꼬리 재귀 함수를 사용해야만 한다.

    fun fib(i: Int): Int = SOLUTION_HERE()
*/
fun ex2n1(){
    fun fib(i: Int): Int {
        if(i<1) throw IllegalArgumentException("자연수 범위의 값을 입력 하세요.")
        tailrec fun loop(i:Int, f:Int, s:Int): Int = when (i) {
            1 -> f
            2 -> s
            else -> loop(i-1, s, f+s)
        }
        return loop(i, 0, 1)
    }

    println("# 연습문제 2.1")
    print("  계산 하고자 하는 피보나치 수의 회차를 입력 하세요. :")
    println("  피보나치 수는 ${fib(Scanner(System.`in`).nextInt())} 입니다.")
    println("# END")
}

/*  연습문제 2.2
    isSorted를 구현하라.
    이 함수는 List<A> 타입의 단일 연결 리스트를 받아서 이 리스트가 주어진 비교 함수에 맞춰 적절히 정렬돼 있는지를 검사한다.
    이 함수의 앞에는 두 가지 확장 프로퍼티 정의가 있다.
    head 프로퍼티는 리스트의 첫 번째 원소를 반환하며, tail 프로퍼티는 List<A>에서 첫 번째 원소를 제외한 나머지 리스트를 반환한다.
    확장 함수에 대한 기억을 되살리고 싶은 독자는 '확장 메서드와 프로퍼티' 박스 설명을 살펴보라.

    val <T> List<T>.tail: List<T>
        get() = drop(1)
    val <T> List<T>.head: T
        get() = first()
    fun <A> isSorted(aa: List<A>, order: (A, A) -> Boolean): Boolean = SOLUTION_HERE()
*/
val <T> List<T>.tail: List<T>
    get() = drop(1)
val <T> List<T>.head: T
    get() = first()

fun ex2n2(){
    fun <A> isSorted(aa: List<A>, order: (A, A) -> Boolean): Boolean {
        tailrec fun loop(a: A, aa: List<A>): Boolean = when {
            aa.isEmpty()->true
            !order(a, aa.head)->false
            else->loop(aa.head, aa.tail)
        }
        return aa.isEmpty() || loop(aa.head, aa.tail)
    }

    println("# 연습문제 2.2")
    println("  list01(a, c, b, a)의 정렬 상태:${isSorted(listOf("a","c","b","a")) { x, y -> x <= y }}")
    println("  list02(1, 2, 3, 4)의 정렬 상태:${isSorted(listOf(1, 2, 3, 4)) { x, y -> x <= y }}")
    println("# END")
}

/*  연습문제 2.3
    다른 예제로 커링을 살펴보자.
    커링은 인자를 두 개 받는 함수 f를 받아서 첫 번째 인자를 f에 부분 적용한 새 함수를 돌려주는 함수다.

    fun <A, B, C> curry(f: (A, B) -> C): (A) -> (B) -> C = SOLUTION_HERE()
*/
fun ex2n3(){
    fun <A, B, C> curry(f: (A, B) -> C): (A) -> (B) -> C = { a -> { b -> f(a,b) } }

    val plusOne = curry { a:Int , b:Int -> a + b } (1)
    println("# 연습문제 2.3")
    println("  plusOne = curry { a:Int , b:Int -> a + b } (1)")
    println("  plusOne(3) = ${plusOne(3)}")
    println("# END")
}

/*  연습문제 2.4
    curry 변환의 역변환인 uncurry를 구현하라.
    ->가 오른쪽 결합이므로 (A) -> ((B) -> c)를 (A) -> (B) -> C라고 적을 수 있다.

    fun <A, B, C> uncurry(f: (A) -> (B) -> C): (A, B) -> C = SOLUTION_HERE()
*/
fun ex2n4(){
    fun <A, B, C> uncurry(f: (A) -> (B) -> C): (A, B) -> C = { a, b -> f(a)(b) }

    val plus = uncurry { a:Int -> { b:Int -> a+b } }
    println("# 연습문제 2.4")
    println("  plus = uncurry { a:Int -> { b:Int -> a+b } }")
    println("  plus(1, 3) = ${plus(1, 3)}")
    println("# END")
}

/*  연습문제 2.5
    두 함수를 합성하는 고차 함수를 작성하라.

    fun <A, B, C> compose(f: (B) -> C, g: (A) -> B): (A) -> C = SOLUTION_HERE()
*/
fun ex2n5(){
    fun <A, B, C> compose(f: (B) -> C, g: (A) -> B): (A) -> C = { a -> f(g(a)) }

    val squareBold = compose({b:String->"<b>$b</b>"}, {a:Int->(a * a).toString()})
    println("# 연습문제 2.5")
    println("  squareBold = ${squareBold(5)}")
    println("# END")
}

fun main(){
    ex2n1()
    ex2n2()
    ex2n3()
    ex2n4()
    ex2n5()
}



