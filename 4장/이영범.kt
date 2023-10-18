/**
 * 리스트 4.1 예외 던지고 받기
 */
fun failingFn(i: Int): Int {
    return try {
        val y: Int = throw Exception("boom")
        val x = 42 + 5
        x + y
    } catch (e: Exception) {
        43
    }
}

fun failingFn2(i: Int): Int {
    return try {
        val x = 42 + 5
        x + (throw Exception("boom!")) as Int
    } catch (e: Exception) {
        43
    }
}

private fun <A> length(xs: List<A>): Int = TODO()
private fun <A> List<A>.size(): Int = TODO()

val listing1 = {
    fun mean(xs: List<Double>): Double =
        if (xs.isEmpty())
            throw ArithmeticException("mean of empty list!")
        else xs.sum() / length(xs)
}

val listing2 = {
    fun mean(xs: List<Double>, onEmpty: Double): Double =
        if (xs.isEmpty()) onEmpty
        else xs.sum() / xs.size()
}


private fun List<Double>.sum(): Double = TODO()
private fun <A> List<A>.size(): Int = TODO()

sealed class Option<out A>

data class Some<out A>(val get: A) : Option<A>()

object None : Option<Nothing>()

fun mean(xs: List<Double>): Option<Double> =
    if (xs.isEmpty()) None
    else Some(xs.sum() / xs.size())

/**
 * 연습문제 4.1
 */
fun <A, B> Option<A>.map(f: (A) -> B): Option<B> =
    when (this) {
        is None -> None
        is Some -> Some(f(this.get))
    }

fun <A> Option<A>.getOrElse(default: () -> A): A =
    when (this) {
        is None -> default()
        is Some -> this.get
    }

fun <A, B> Option<A>.flatMap(f: (A) -> Option<B>): Option<B> =
    this.map(f).getOrElse { None }

fun <A, B> Option<A>.flatMap2(f: (A) -> Option<B>): Option<B> =
    when (this) {
        is None -> None
        is Some -> f(this.get)
    }

fun <A> Option<A>.orElse(ob: () -> Option<A>): Option<A> =
    this.map { Some(it) }.getOrElse { ob() }

fun <A> Option<A>.orElse2(ob: () -> Option<A>): Option<A> =
    when (this) {
        is None -> ob()
        is Some -> this
    }

fun <A> Option<A>.filter(f: (A) -> Boolean): Option<A> =
    this.flatMap { a -> if (f(a)) Some(a) else None }

fun <A> Option<A>.filter2(f: (A) -> Boolean): Option<A> =
    when (this) {
        is None -> None
        is Some ->
            if (f(this.get)) this
            else None
    }

data class Employee(
    val name: String,
    val department: String,
    val manager: Option<String>
)

fun lookupByName(name: String): Option<Employee> = TODO()
fun timDepartment(): Option<String> =
    lookupByName("Tim").map { it.department }

val unwieldy: Option<Option<String>> = lookupByName("Tim").map { it.manager }

val manager: String = lookupByName("Tim")
    .flatMap { it.manager }
    .getOrElse { "Unemployed" }

/**
 * 연습문제 4.2
 */
fun variance(xs: List<Double>): Option<Double> =
    mean(xs).flatMap { m ->
        mean(xs.map { x ->
            (x - m).pow(2)
        })
    }

val dept: String = lookupByName("Tim")
    .map { it.department }
    .filter { it != "Accounts" }
    .getOrElse { "Unemployed" }

/**
 * 리스트 4.4 함수를 Option에 대해 작용하도록 끌어올리기
 */
fun <A, B> lift(f: (A) -> B): (Option<A>) -> Option<B> =
    { oa -> oa.map(f) }

// lift를 사용해 kotlin.math.abs 내장 함수를 변환
val abs0: (Option<Double>) -> Option<Double> =
    lift { kotlin.math.abs(it) }

/**
 * 두 가지 핵심 요소로부터 연간 보험 할일윤을 계산하는 최고 기밀 공식
 */
fun insuranceRateQuote(
    age: Int,
    numberOfSpedingTickets: Int
): Double = TODO()

fun <A> catches(a: () -> A): Option<A> =
    try {
        Some(a())
    } catch (e: Throwable) {
        None
    }

/**
 * 연습문제 4.3
 * 두 Option 값을 이항 함수를 통해 조합하는 제네릭 함수 map2 작성
 * 두 Option 중 어느 하나라도 None이면 반환값도 None
 */
fun <A, B, C> map2(oa: Option<A>, ob: Option<B>, f: (A, B) -> C): Option<C> =
    oa.flatMap { a ->
        ob.map { b ->
            f(a, b)
        }
    }

fun parseInsuranceRateQuote(
    age: String,
    speedingTickets: String
): Option<Double> {

    val optAge: Option<Int> = catches { age.toInt() }

    val optTickets: Option<Int> =
        catches { speedingTickets.toInt() }

    return map2(optAge, optTickets) { a, t ->
        insuranceRateQuote(a, t)
    }
}

/**
 * 연습문제 4.4
 * 원소가 Option인 리스트가 원소가 리스트인 Option으로 합쳐주는 sequence 함수를 작성
 * 반환되는 Option의 원소는 원래 리스트에서 Some인 값들만 모은 리스트
 * 원래 리스트 안에 None이 하나라도 있으면 결괏값이 Non, 그렇지 않으면 모든 정상값이 모인 리스트가 들어있는 Some이 결괏값
 */
fun <A> sequence(xs: List<Option<A>>): Option<List<A>> =
    xs.foldRight(Some(Nil),
        { oa1: Option<A>, oa2: Option<List<A>> ->
            map2(oa1, oa2) { a1: A, a2: List<A> ->
                Cons(a1, a2)
            }
        })

/**
 * 연습문제 4.5
 */
fun <A, B> traverse(xa: List<A>, f: (A) -> Option<B>): Option<List<B>> =
    when (xa) {
        is Nil -> Some(Nil)
        is Cons ->
            map2(f(xa.head), traverse(xa.tail, f)) { b, xb ->
                Cons(b, xb)
            }
    }

fun <A> sequence2(xs: List<Option<A>>): Option<List<A>> =
    traverse(xs) { it }

/**
 * 연습문제 4.6
 */
fun <E, A, B> Either<E, A>.map(f: (A) -> B): Either<E, B> =
    when (this) {
        is Left -> this
        is Right -> Right(f(this.value))
    }

fun <E, A> Either<E, A>.orElse(f: () -> Either<E, A>): Either<E, A> =
    when (this) {
        is Left -> f()
        is Right -> this
    }


fun <E, A, B, C> map3(ae: Either<E, A>, be: Either<E, B>, f: (A, B) -> C): Either<E, C> =
    ae.flatMap { a -> be.map { b -> f(a, b) } }

/**
 * 연습문제 4.7
 */
fun <E, A, B> traverse2( xs : FList<A>, f: (A) -> Either<E, B> ): Either<E, FList<B>> =
    when (xs) {
        is Nil -> Right(Nil)
        is Cons ->
            map3(f(xs.head), traverse2(xs.tail, f)) { b, xb ->
                Cons(b, xb)
            }
    }

fun <E, A> sequence(es: FList<Either<E, A>>): Either<E, FList<A>> = traverse2(es) {it}

/**
 * 연습문제 4.8
 */
sealed class Partial<out A, out B>

data class Failures<out A>(val get: List<A>): Partial<A, Nothing>()
data class Success<out B>(val get: B): Partial<Nothing, B>()