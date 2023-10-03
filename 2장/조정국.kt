/*
 * 연습문제 2.1 : n 번째 피보나치 수열 구하기
 * - 처음 두 피보나치 수는 0과 1.
 * - n번째 피보나치 수는 자신 바로 앞의 두 피보나치 수의 합 : f(n)->
 * - 피보나치 수열은 0, 1, 1, 2, 3, 5, 8, 13, 21 등.
 * - 함수 정의 : 지역적인 꼬리 재귀 함수를 사용
 * */
fun fib(i: Int): Int {
    tailrec fun go(n: Int, f: Int, s: Int): Int =
        if (n > 0)
            go(n-1, s, f+s)
        else s
    return go(i, 0, 1)
}

fun callEx0201(){
    for (i in 0 until 10) {
        println("> fib["+i+"] : "+fib(i))
    }
}

/*
 * 연습문제 2.2 : isSorted 구현
 *  - List 타입의 단일 연결 리스트를 받아
 *  - 이 리스트가 주어진 비교 함수에 맞춰 적절히 정렬돼 있는지 검사
 *  - 이 함수의 앞에 두 가지 확장프로퍼티[extension property] 정의
 *     > head 프로퍼티 : 리스트의 첫 번째 원소 반환
 *     > tail 프로퍼티 : List에서 첫 번째 원소를 제외한 나머지 리스트 반환
 * */
val <T> List<T>.tail: List<T>
    get() = drop(1)

val <T> List<T>.head: T
    get() = first()

fun <A> isSorted(aa: List<A>, order: (A, A) -> Boolean): Boolean  {
    if(aa.size <= 1) return true

    fun loop(h:A, t:List<A>): Boolean =
        when{
            t.isEmpty() -> true
            !order(h,t.head) -> false
            else -> loop(t.head, t.tail)
        }

    return aa.isEmpty() || loop(aa.head, aa.tail)
}

fun callEx0202(){
    val lamInt = { a: Int, b: Int -> a <= b }
    val lamTxt = { a: String, b: String -> a <= b }

    val testEmpty = listOf("")
    println("test : "+testEmpty+" sorted is "+ isSorted(testEmpty , lamTxt))

    val testIntTrue = listOf(1,2,3)
    println("test : "+testIntTrue+" sorted is "+ isSorted(testIntTrue , lamInt))

    val testIntFalse = listOf(1,2,3,1)
    println("test : "+testIntFalse+" sorted is "+ isSorted(testIntFalse , lamInt))

    val testTxtTrue = listOf("a", "b", "n")
    println("test : "+testTxtTrue+" sorted is "+ isSorted(testTxtTrue , lamTxt))

    val testTxtFalse = listOf("a", "z", "n")
    println("test : "+testTxtFalse+" sorted is "+ isSorted(testTxtFalse , lamTxt))
}

/*
 * 연습문제 2.3 : curry
 * */
fun <A, B, C> curry(f: (A, B) -> C): (A) -> (B) -> C = {
        a: A -> { b: B -> f(a, b) }
}

fun callEx0203(){
    println("curry.String[1,2] -> " + ex02.ex0203.curry { a: Long, b: Int -> "$a$b" }(1)(2))
    println("curry.int[1,2] -> " + ex02.ex0203.curry(fun(a: Long, b: Int) = a + b)(1)(2))
}

/*
 * 연습문제 2.4 : uncurry
 * */
fun <A, B, C> uncurry(f: (A) -> (B) -> C): (A, B) -> C = {
        a: A, b: B -> f(a)(b)
}

fun callEx0204(){
    println("uncurry.String[1,2] -> " + ex02.ex0203.uncurry { a: Int -> { b: Long -> "$a$b" } }(1, 2))
    println("uncurry.int[1,2] -> " + ex02.ex0203.uncurry(fun(a: Int) = fun(b: Long) = a + b)(1, 2))
}

/*
 * 연습문제 2.5 : compose
 * */
fun <A, B, C> compose(f: (B) -> C, g: (A) -> B): (A) -> C = {
        a: A -> f(g(a))
}

fun callEx0205(){
    println("compose.String[3] -> " + ex02.ex0203.compose({ b: Int -> "$b" + 2 }, { a: Long -> a.toInt() * 100 })(3))
    println("compose.int[3] -> " + ex02.ex0203.compose(fun(b: Int) = b + 2, fun(a: Long) = a.toInt() * 100)(3))
}


fun main() {
    println("############ 연습문제 2.1 ############")
    callEx0201()

    println("############ 연습문제 2.2 ############")
    callEx0202()

    println("############ 연습문제 2.3 ############")
    callEx0203()

    println("############ 연습문제 2.4 ############")
    callEx0204()

    println("############ 연습문제 2.5 ############")
    callEx0205()
}
