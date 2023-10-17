import Option.None
import Option.Some
import kotlin.math.pow

sealed class Option<out ITEM: Any> {
    data class Some<out ITEM: Any>(val item: ITEM) : Option<ITEM>()
    data object None : Option<Nothing>()

    companion object {
        operator fun <ITEM: Any> invoke(item: ITEM) = Some(item)
        fun <ITEM: Any> none(): Option<ITEM> = None
    }
}

/*
연습문제 4.2
앞에 있는 Option에 대한 모든 함수를 구현하라.
각 함수를 구현할 때 각 함수의 의미가 무엇이고 어떤 상황에서 각 함수를 사용하지 생각해보라.
나중에 각 함수를 언제 사용할지 살펴본다.
다음은 이 연습문제를 풀기 위한 몇 가지 힌트다.

- 매칭을 사용해도 좋다. 하지만 map과 getOrElse 이외의 모든 함수를 매칭 없이 구현 할 수 있다.
- map과 flapMap의 경우 타입 시그니처만으로 구현을 결정할 수 있다.
- getOrElse는 Option이 Some인 경우 결과를 반환하지만 Option이 None인 경우 주어진 디폴트 값을 반환한다.
- orElse는 첫 번째 Option의 값이 정의된 경우(즉, Some인 경우) 그 Option을 반환한다.
  그렇지 않은 경우 두 번째 Option을 반환한다.
*/

fun <ITEM: Any, OTHER: Any> Option<ITEM>.map(
    f: (ITEM) -> OTHER
): Option<OTHER> = when (this) {
    is Some -> Some(f(item))
    is None -> None
}

fun <ITEM: Any> Option<ITEM>.getOrElse(
    default: () -> ITEM
): ITEM = when (this) {
    is Some -> item
    is None -> default()
}

fun <ITEM: Any, OTHER: Any> Option<ITEM>.flatMap(
    f: (ITEM) -> Option<OTHER>
): Option<OTHER> = map(f).getOrElse { None }

fun <ITEM: Any> Option<ITEM>.orElse(
    ob: () -> Option<ITEM>
): Option<ITEM> = map() { Option(it) }.getOrElse { ob() }

fun <ITEM: Any> Option<ITEM>.filter(
    f: (ITEM) -> Boolean
): Option<ITEM> = flatMap { v ->
    if (f(v)) Option(v) else None
}

/*
연습문제 4.2
flatMap을 사용해 variance 함수를 구현하라.
시퀀스의 평균이 m이면, 분산은 시퀀스의 원소를 x라 할 때 x-m을 제곱한 값의 평균이다.
코드로 쓰면(x - m).pow(2)라 할 수 있다.
리스트 4.2에서 만든 mean 메서드를 사용해 이 함수를 구현할 수 있다.
*/
fun mean(xs: FList<Double>): Option<Double> = if (xs == FList<Double>()) None else Option(xs.sum() / xs.size)

fun variance(xs: FList<Double>): Option<Double> = mean(xs).flatMap { v ->
    mean(xs.map { (it - v).pow(2) })
}

/*
연습문제 4.3
두 Option값을 이항 함수를 통해 조립하는 제네릭 함수 map2를 작성하라.
두 Option중 어느 하나라도 None이면 반환값도 None이다.
다음은 map2의 시그니처다.
*/
fun <P1: Any, P2: Any, RESULT: Any> map2(
    p1: Option<P1>,
    p2: Option<P2>,
    f: (P1, P2) -> RESULT
): Option<RESULT> = p1.flatMap { v1 ->
    p2.map { v2 -> f(v1, v2) }
}

fun <ITEM: Any, P: Any, RESULT: Any> Option<ITEM>.mapTow(
    b: Option<P>,
    f: (ITEM, P) -> RESULT
): Option<RESULT> = flatMap { v1 ->
    b.map { v2 -> f(v1, v2) }
}

/*
연습문제 4.4
원소가 Option인 리스트를 원소가 리스트인 Option으로 합쳐주는 sequence 함수를 작성하라.
반환되는 Option의 원소는 원래 리스트에서 Some인 값들만 모은 리스트다.
원래 리스트 안에 None이 단 하나라도 있으면 결괏값이 None이어야 하며,
그렇지 않으면 모든 정상 값이 모인 리스트가 들어 있는 Some이 결과값이어야 한다.
시그니처는 다음과 같다.
*/
fun <ITEM: Any> sequence(xs: FList<Option<ITEM>>): Option<FList<ITEM>> =
    xs.foldRight(Option(FList())) { oa1: Option<ITEM>, oa2: Option<FList<ITEM>> ->
        map2(oa1, oa2) { a1: ITEM, a2: FList<ITEM> -> FList(a1).append(a2) }
    }

/*
연습문제 4.5
traverse 함수를 구현하라.
map을 한 다음에 sequence를 하면 간단하지만, 리스트를 단 한번만 순회하는 더 효율적인 구현을 시도해보라.
코드를 작성하고 나면 sequence를 traverse를 사용해 구현하라.
*/
fun <ITEM : Any, OTHER : Any> traverse(
    xa: FList<ITEM>,
    f: (ITEM) -> Option<OTHER>
): Option<FList<OTHER>> = xa.foldRight(Option(FList())) { curr, acc ->
    val list = f(curr).map { FList(it) }.getOrElse { FList() }
    acc.map { list.append(it) }
}
//= when (xa) {
//    is FList.Cons ->
//        map2(f(xa.head), traverse(xa.tail, f)) { b, xb ->
//            FList(b).append(xb)
//        }
//
//    is FList.Nil -> Option(FList.Nil)
//}

fun <ITEM : Any> sequence2(xs: FList<Option<ITEM>>): Option<FList<ITEM>> = traverse(xs) { it }

/*
연습문제 4.6
Right값에 대해 활용할 수 있는 map, flatMap, orElse, map2를 구현하라.
*/
sealed class Either<out LEFT, out RIGHT>
data class Left<out LEFT>(val value: LEFT) : Either<LEFT, Nothing>()
data class Right<out RIGHT>(val value: RIGHT) : Either<Nothing, RIGHT>()

fun <LEFT, RIGHT, OTHER> Either<LEFT, RIGHT>.map(f: (RIGHT) -> OTHER): Either<LEFT, OTHER> = when (this) {
    is Left -> this
    is Right -> Right(f(value))
}

fun <LEFT, RIGHT, OTHER> Either<LEFT, RIGHT>.flatMap(
    f: (RIGHT) -> Either<LEFT, OTHER>
): Either<LEFT, OTHER> = when (this) {
    is Left -> this
    is Right -> f(value)
}

fun <LEFT, RIGHT> Either<LEFT, RIGHT>.orElse(
    f: () -> Either<LEFT, RIGHT>
): Either<LEFT, RIGHT> = when (this) {
    is Left -> f()
    is Right -> this
}

fun <LEFT, RIGHT1, RIGHT2, OTHER> mapEither(
    e1: Either<LEFT, RIGHT1>,
    e2: Either<LEFT, RIGHT2>,
    f: (RIGHT1, RIGHT2) -> OTHER
): Either<LEFT, OTHER> = e1.flatMap { r1 -> e2.map { r2 -> f(r1, r2) } }

/*
연습문제 4.7
Either에 대한 sequence와 traverse를 구현하라.
두 함수는 오류가 생긴 경우 최초로 발생한 오류를 반환해야 한다.
*/
fun <LEFT: Any, RIGHT: Any> Either<LEFT, RIGHT>.getOrElse(
    default: () -> RIGHT
): RIGHT = when (this) {
    is Left -> default()
    is Right -> value
}

fun <LEFT: Any, ITEM : Any, OTHER : Any> traverseEither(
    xs: FList<ITEM>,
    f: (ITEM) -> Either<LEFT, OTHER>
): Either<LEFT, FList<OTHER>> = xs.foldRight(Right(FList())) { curr, acc ->
    val list = f(curr).map { FList(it) }.getOrElse { FList() }
    acc.map { list.append(it) }
}
//= when (xs) {
//    is FList.Nil -> Right(FList())
//    is FList.Cons -> mapEither(f(xs.head), traverseEither(xs.tail, f)) { b, xb ->
//        FList(b).append(xb)
//    }
//}

fun <LEFT : Any, RIGHT : Any> sequenceEither(es: FList<Either<LEFT, RIGHT>>): Either<LEFT, FList<RIGHT>> =
    traverseEither(es) { it }

/*
연습문제 4.8
리스트 4.8에서는 이름과 나이가 모두 잘못되더라도 map2가 오류를 하나만 보고할 수 있다.
두 오류를 모두 보고하게 하려면 어디를 바꿔야 할까?
map2나 mkPerson의 시그니처를 바꿔야 할까, 아니면 Either보다 이 추가 요구 사항을 더 잘 다룰 수 있는
추가 구조를 포함하는 새로운 데이터 타입을 만들어야 할까?
이 데이터 타입에 대해 orElse, traverse, sequence는 어떻게 다르게 동작해야 할까?
*/
data class Name(val value: String)
data class Age(val value: Int)
data class Person(val name: Name, val age: Age)

fun mkName(name: String): Either<String, Name> =
    if(name.isBlank()) Left("Name is Empty.")
    else Right(Name(name))

fun mkAge(age: Int): Either<String, Age> =
    if(age < 0) Left("Age is out of range.")
    else Right(Age(age))

fun mkPerson(name: String, age: Int): Either<String, Person> =
    mapEither(mkName(name), mkAge(age)) { n, a -> Person(n, a) }



// ===== Test =====

class Chapter4Test {
    @Test
    fun test4_1() {
        val some= Option(1)
        val none= Option.none<Int>()
        assertEquals(some.map() { "$it" }, Option("1"))
        assertEquals(some.getOrElse() { 0 }, 1)
        assertEquals(none.getOrElse() { 0 }, 0)
        assertEquals(some.flatMap() { Option("$it") }, Option("1"))
        assertEquals(none.flatMap() { Option("$it") }, Option.none())
        assertEquals(some.orElse() { Option(2) }, Option(1))
        assertEquals(none.orElse() { Option(2) }, Option(2))
        assertEquals(some.filter() { it == 1 }, Option(1))
        assertEquals(none.filter() { it == 1 }, none)
    }

    @Test
    fun test4_2() {
        val list = FList(1.0, 2.0, 3.0)
        assertEquals(mean(list), Option(2.0))
        assertEquals(variance(list), Option(0.6666666666666666))
    }

    @Test
    fun test4_3() {
        val some= Option(1)
        val none= Option.none<Int>()
        assertEquals(map2(some, Option(2)) { a, b -> a + b }, Option(3))
        assertEquals(map2(some, none) { a, b -> a + b }, Option.none())
        assertEquals(some.mapTow(Option(2)) { a, b -> a + b }, Option(3))
    }

    @Test
    fun test4_4() {
        val listOption = FList(Option(1), Option(2), Option(3))
        val optionList = Option(FList(1, 2, 3))
        assertEquals(sequence(listOption), optionList)
        assertEquals(traverse(listOption) {it}, optionList)
    }

    @Test
    fun test4_5() {
        val listOption = FList(Option(1), Option(2), Option(3))
        val optionList = Option(FList(1, 2, 3))
        assertEquals(sequence2(listOption), optionList)
    }

    @Test
    fun test4_6() {
        assertEquals(Left(1).map {  }, Left(1))
        assertEquals(Right(1).map { it + 2 }, Right(3))
        assertEquals(Right(1).flatMap { Right(it + 2) }, Right(3))
        assertEquals(Right(1).orElse { Right(2) }, Right(1))
        assertEquals(mapEither(Right(1), Right(2)) { r1, r2 -> r1 + r2}, Right(3))
    }

    @Test
    fun test4_7() {
        val listEither = FList(Right(1), Right(2), Right(3))
        val eitherList = Right(FList(1, 2, 3))
        assertEquals(traverseEither(listEither) {it}, eitherList)
        assertEquals(sequenceEither(listEither), eitherList)
    }

    @Test
    fun test4_8() {
        val name = mkName("hong gil dong")
        val age = mkAge(11)
        val person = mkPerson("hong gil dong", 11)
        assertEquals(name, Right(Name("hong gil dong")))
        assertEquals(age, Right(Age(11)))
        assertEquals(person, Right(Person(Name("hong gil dong"), Age(11))))
    }
}
