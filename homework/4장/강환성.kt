package khs

import khs.OptionF.None
import khs.OptionF.Some
import kotlin.math.pow

fun mean(lst: List<Double>): Double
= if(lst.isEmpty()) throw ArithmeticException("mean of empty list")
else lst.sum() / lst.size

sealed class OptionF<out ITEM:Any> {
    data class Some<out ITEM:Any>(val get: ITEM): OptionF<ITEM>()
    data object None: OptionF<Nothing>()

    companion object {
        fun <ITEM:Any> none() :OptionF<ITEM> =None
        fun <ITEM:Any> some(v:ITEM) :OptionF<ITEM> =Some(v)
    }
}

/**
 * 연습문제 4.1
 * 앞에 있는 Option에 대한 모든 함수를 구현하라.
 * 각 함수를 구현할 때 각 함수의 의미가 무엇이고 어떤 상황에서 각 함수를 사용할지 생각해보라.
 * 나중에 각 함수를 언제 사용할지 살펴본다.
 * 다음은 이 연습문제를 풀기 위한 몇 가지 힌트다.
 * - 매칭을 사용해도 좋다. 하지만 map과 getOrElse 이외의 모든 함수를 매칭 없이 구현 할 수 있다.
 * - map과 flatMap의 경우 타입 시그니처만으로 구현을 결정할 수 있다.
 * - getOrElse는 Option이 Some인 경우 결과를 반환하지만 Option이 None인 경우 주어진 디폴트 값을 반환한다.
 * - orElse는 첫 번째 Option의 값이 정의된 경우(즉, Some인 경우) 그 Option을 반환한다.
 *   그렇지 않은 경우 두 번째 Option을 반환한다.
 */
fun <ITEM:Any, OTHER:Any> OptionF<ITEM>.map(block :(ITEM) ->OTHER) :OptionF<OTHER>
= when(this) {
    is None ->None
    is Some ->Some(block(get))
}

fun <ITEM:Any, OTHER:Any> OptionF<ITEM>.flatMap(block :(ITEM) ->OptionF<OTHER>) :OptionF<OTHER>
= map(block).getOrElse { None }

fun <ITEM:Any> OptionF<ITEM>.getOrElse(block :() ->ITEM) :ITEM
= when(this) {
    is None ->block()
    is Some ->get
}

fun <ITEM:Any> OptionF<ITEM>.orElse(block :() ->OptionF<ITEM>) :OptionF<ITEM>
= map { Some(it) }.getOrElse { block() }

fun <ITEM:Any> OptionF<ITEM>.filter(block :(ITEM) ->Boolean) :OptionF<ITEM>
= flatMap { if(block(it)) Some(it) else None }

/**
 * 연습문제 4.2
 * flatMap을 사용해 variance 함수를 구현하라.
 * 시퀀스의 평균이 m 이면, 분산은 시퀀스의 원소를 x라 할 때 x-m을 제곱한 값의 평균이다.
 * 코드로 쓰면(x - m).pow(2)라 할 수 있다.
 * 리스트 4.2에서 만든 mean 메서드를 사용해 이 함수를 구현할 수 있다.
 */
fun OptionF.Companion.mean(list :List<Double>) :OptionF<Double>
= if(list.isEmpty()) None else Some(list.sum() / list.size)

fun OptionF.Companion.variance(list :List<Double>) :OptionF<Double>
= mean(list).flatMap {m ->
    mean(list.map {(it - m).pow(2)})
}

// 리스트 4.4 함수를 Option에 대해 작용하도록 끌어올리기
fun <ITEM:Any, OTHER:Any> OptionF.Companion.lift(block :(ITEM) ->OTHER) :(OptionF<ITEM>) ->OptionF<OTHER>
= { it.map(block) }
val absO :(OptionF<Double>) ->OptionF<Double> =OptionF.lift { kotlin.math.abs(it) }

fun insuranceRateQuote(age :Int, numberOfSpeedingTickets: Int) :Double =age *0.1 +numberOfSpeedingTickets
fun parseInsuranceRateQuote(age :String, speedingTickets :String) :OptionF<Double> {
    val optAge :OptionF<Int> =OptionF.catches {age.toInt()}
    val optTickets :OptionF<Int> =OptionF.catches { speedingTickets.toInt() }
//    return Some(insuranceRateQuote(optAge.getOrElse { 10 }, optTickets.getOrElse { 0 }))
    return optAge.map2(optTickets) {a, b -> insuranceRateQuote(a, b)}
}
fun <ITEM:Any> OptionF.Companion.catches(block :() ->ITEM) :OptionF<ITEM>
= try {
    Some(block())
} catch (e :Throwable) {
    None
}

/**
 * 연습문제 4.3
 * 두 Option 값을 이항 함수를 통해 조합 하는 제네릭 함수 map2를 작성하라.
 * 두 Option 중 어느 하나 라도 None이면 반환값도 None이다.
 * 다음은 map2의 시그니처다.
 */
fun <ITEM1:Any, ITEM2:Any, OTHER:Any> OptionF<ITEM1>.map2(op :OptionF<ITEM2>, block :(ITEM1, ITEM2) ->OTHER) :OptionF<OTHER>
= flatMap { op.map {a ->block(it, a) } }
//= when(this) {
//    is None ->None
//    is Some ->when(op) {
//        is None ->None
//        is Some ->Some(block(get, op.get))
//    }
//}

/**
 * 연습문제 4.4
 * 원소가 Option인 리스트를 원소가 리스트인 Option으로 합쳐주는 sequence 함수를 작성하라.
 * 반환되는 Option의 원소는 원래 리스트에서 Some인 값들만 모은 리스트다.
 * 원래 리스트 안에 None이 단 하나라도 있으면 결괏값이 None이어야 하며,
 * 그렇지 않으면 모든 정상 값이 모인 리스트가 들어 있는 Some이 결괏값이어야 한다.
 * 시그니처는 다음과 같다.
 * 이 문제는 객체지향 스타일로 함수를 작성하는 게 적합하지 않다고 확실히 알 수 있는 경우다.
 * 이 함수는 List의 메서드가 돼서는 안 되고(List는 Option에 대해 알 필요가 없어야만 한다).
 * Option의 메서드가 될 수도 없다.
 * 따라서 이 함수를 Option의 동반 객체 안에 넣어야 한다.
 */
fun <ITEM:Any> OptionF.Companion.sequence(list: ListF<OptionF<ITEM>>) :OptionF<ListF<ITEM>>
= list.foldRight(Some(ListF.empty())) {it, acc ->
    it.map2(acc) {a, b ->ListF.of(a).append(b)}
}

/**
 * 연습문제 4.5
 * traverse 함수를 구현하라.
 * map을 한 다음에 sequence를 하면 간단하지만,
 * 리스트를 단 한번만 순회하는 더 효율적인 구현을 시도해보라.
 * 코드를 작성하고 나면 sequence를 traverse를 사용해 구현하라
 */
// ====
fun parseInts(lst :ListF<String>) :OptionF<ListF<Int>>
=OptionF.sequence(lst.map {OptionF.catches { it.toInt() }})

fun <ITEM:Any, OTHER:Any> traverse(lst: ListF<ITEM>, block :(ITEM) ->OptionF<OTHER>) :OptionF<ListF<OTHER>>
= when(lst) {
    is ListF.Nil ->Some(ListF.empty())
    is ListF.Cons ->lst.foldRight(Some(ListF.empty())) {it, acc ->
        block(it).flatMap { acc.map {a ->ListF.of(it).append(a)} }
    }
}
fun <ITEM:Any> OptionF.Companion.sequence2(list: ListF<OptionF<ITEM>>) :OptionF<ListF<ITEM>>
= traverse(list) {it}


package khs

import khs.EitherF.Left
import khs.EitherF.Right

sealed class EitherF<out L:Any, out R:Any> {
    data class Left<out L:Any>(val value :L) :EitherF<L, Nothing>()
    data class Right<out R:Any>(val value :R) :EitherF<Nothing, R>()

    companion object {
        fun <L:Any> left(value:L) :EitherF<L,Nothing> =Left(value)
        fun <R:Any> right(value:R) :EitherF<Nothing,R> =Right(value)
    }
}

/**
 * 연습문제 4.6
 * Right 값에 대해 활용할 수 있는 map, flatMap, orElse, map2를 구현하라.
 */
fun <L:Any, R:Any, OTHER:Any> EitherF<L, R>.map(block :(R) ->OTHER) :EitherF<L, OTHER>
=when(this) {
    is Left ->this
    is Right ->Right(block(value))
}

fun <L:Any, R:Any, OTHER:Any> EitherF<L,R>.flatMap(block :(R) ->EitherF<L,OTHER>) :EitherF<L,OTHER>
=when(this) {
    is Left ->this
    is Right ->block(value)
}

fun <L:Any, R:Any> EitherF<L,R>.orElse(block :() ->EitherF<L,R>) :EitherF<L,R>
=when(this) {
    is Left ->block()
    is Right ->this
}

fun <L:Any, R1:Any, R2:Any, OTHER:Any> EitherF<L,R1>.map2(
    other :EitherF<L,R2>,
    block :(R1, R2) ->OTHER
) :EitherF<L,OTHER> =flatMap {r1 ->other.map {r2 ->block(r1, r2)}}
//=when(this) {
//    is Left ->this
//    is Right ->when(other) {
//        is Left ->other
//        is Right ->Right(block(value, other.value))
//    }
//}

/**
 * 연습문제 4.7
 * Either에 대한 sequence와 traverse를 구현하라.
 * 두 함수는 오류가 생긴 경우 최초로 발생한 오류를 반환해야 한다.
 */
fun <L:Any, R:Any, OTHER:Any> EitherF.Companion.traverse(
    list :ListF<R>,
    block :(R) ->EitherF<L,OTHER>
) :EitherF<L, ListF<OTHER>>
=when(list) {
    is ListF.Nil ->Right(ListF.empty())
    is ListF.Cons ->
        block(list.head).map2(traverse(list.tail, block)) {r1, r2 ->
            ListF.of(r1).append(r2)
        }
}
fun <L:Any, R:Any> EitherF.Companion.sequence(list :ListF<EitherF<L,R>>) :EitherF<L, ListF<R>>
=traverse(list) {it}

/**
 * 연습문제 4.8
 * 리스트 4.8에서는 이름과 나이가 모두 잘못되더라도 map2가 오류를 하나만 보고할 수 있다.
 * 두 오류를 모두 보고하게 하려면 어디를 바꿔야 할까?
 * map2나 mkPerson의 시그니처를 바꿔야 할까, 아니면 Either보다 이 추가 요구 사항을 더 잘 다룰 수 있는
 * 추가 구조를 포함하는 새로운 데이터 타입을 만들어야 할까?
 * 이 데이터 타입에 대해 orElse, traverse, sequence는 어떻게 다르게 동작 해야 할까?
 */
data class Name(val value :String)
data class Age(val value :Int)
data class Person(val name :Name, val age :Age)

fun mkName(name :String) :EitherF<String, Name>
=if(name.isBlank()) Left("Name is Empty") else Right(Name(name))
fun mkAge(age :Int) :EitherF<String, Age>
=if(age <0) Left("Age is out of range") else Right(Age(age))
fun mkPerson(name :String, age :Int) :EitherF<String, Person>
=mkName(name).map2(mkAge(age)) {r1, r2 ->Person(r1, r2)}



// ===== 4장 테스트 ====
package khs

import kotlin.test.Test
import kotlin.test.assertEquals

class Chapter4Test {
    @Test
    fun optionTest() {
        val none =OptionF.none<Int>()
        val some =OptionF.some(10)

        println("=====> option: $none, $some")
        assertEquals(none.getOrElse { 0 }, 0);
        assertEquals(some.getOrElse { 0 }, 10)
        assertEquals(none.map{it+1}, OptionF.none())
        assertEquals(some.map{it+1}, OptionF.some(11))
        assertEquals(none.flatMap {if(it<=10) OptionF.some(it) else OptionF.none()}, OptionF.none())
        assertEquals(some.flatMap {if(it<=10) OptionF.some(it) else OptionF.none()}, OptionF.some(10))
        assertEquals(none.orElse {OptionF.some(1)}, OptionF.some(1))
        assertEquals(some.orElse {OptionF.some(1)}, OptionF.some(10))
        assertEquals(none.filter {it<=10}, OptionF.none())
        assertEquals(some.filter {it<=10}, OptionF.some(10))
        assertEquals(OptionF.variance(listOf(1.0,2.0,3.0,4.0)), OptionF.some(1.25))
        assertEquals(absO(OptionF.some(-1.0)), OptionF.some(1.0))
        assertEquals(none.map2(OptionF.some(1)) {it, a -> it+a}, OptionF.none())
        assertEquals(some.map2(OptionF.some(1)) {it, a -> it+a}, OptionF.some(11))
        assertEquals(OptionF.sequence(ListF.of(OptionF.some(1), OptionF.none(), OptionF.some(3))), OptionF.none())
        assertEquals(OptionF.sequence(ListF.of(OptionF.some(1), OptionF.some(2), OptionF.some(3))), OptionF.Some(ListF.of(1,2,3)))
        assertEquals(parseInts(ListF.of("1","2")), OptionF.some(ListF.of(1,2)))
        assertEquals(traverse(ListF.of("1","2")) {OptionF.some(it.toInt())}, OptionF.some(ListF.of(1,2)))
    }

    @Test
    fun eitherTest() {
        val left =EitherF.left(1)
        val right =EitherF.right(2)
        println("=====> either: $left, $right")
        assertEquals(left.map{"$it"}, EitherF.left(1))
        assertEquals(right.map{"$it"}, EitherF.right("2"))
        assertEquals(left.flatMap{EitherF.left(10)}, EitherF.left(1))
        assertEquals(right.flatMap{EitherF.right(20)}, EitherF.right(20))
        assertEquals(left.orElse{EitherF.right(10)}, EitherF.right(10))
        assertEquals(right.orElse{EitherF.right(20)}, EitherF.right(2))
        assertEquals(left.map2(EitherF.left(10)){a:Int, b:Int-> a+b}, EitherF.left(1))
        assertEquals(right.map2(EitherF.right(20)){a:Int, b:Int-> a+b}, EitherF.right(22))
        assertEquals(EitherF.sequence(ListF.of(EitherF.right(1), EitherF.right(2))), EitherF.right(ListF.of(1,2)))
    }
}