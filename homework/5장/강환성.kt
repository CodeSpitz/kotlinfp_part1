package khs

import khs.StreamF.Cons
import khs.StreamF.Empty

sealed class StreamF<out ITEM:Any> {
    companion object {
        fun <ITEM :Any> of(vararg xs:ITEM): StreamF<ITEM>
        =if (xs.isEmpty()) Empty else Cons({ xs[0] }, { of(*xs.sliceArray(1 until xs.size)) })

        fun <ITEM:Any> empty() :StreamF<ITEM> =Empty

        fun <ITEM:Any> cons(h :() ->ITEM, t :() ->StreamF<ITEM>) :StreamF<ITEM> {
            val head :ITEM by lazy(h)
            val tail :StreamF<ITEM> by lazy(t)
            return Cons({head}) {tail}
        }
    }

    data class Cons<out ITEM:Any>(
        val head :() ->ITEM,
        val tail :() ->StreamF<ITEM>
    ) :StreamF<ITEM>()
    object Empty :StreamF<Nothing>()
}

fun <ITEM:Any> StreamF<ITEM>.headOption() :OptionF<ITEM>
=when(this) {
    is Empty ->OptionF.none()
    is Cons ->OptionF.some(head())
}
fun <ITEM:Any> StreamF<ITEM>.isEmpty() :Boolean =this is Empty

/**
 * 연습문제 5.1
 * Stream을 List로 변환하는 함수를 작성하라.
 * 이 함수는 스트림의 모든 값을 강제 계산해 REPL에서 결과를 관찰할 수 있게 해준다.
 * 스트림을 3장에서 개발한 단일 연결 List로 변환 해도 된다.
 * 그리고 이 함수나 다른 도우미 함수를 Stream의 확장 메서드로 작성해도 좋다.
 * 이 함수를 구현할 때 스택 안전성을 고려하라.
 * List에서 구현한 다른 메서드를 사용하거나 꼬리 재귀 제거를 고려하라.
 */
fun <ITEM:Any> StreamF<ITEM>.toList() :ListF<ITEM>
=goToList(this, ListF.empty()).reverse()
//=when(this) {
//    is Empty ->ListF.empty()
//    is Cons ->ListF.cons(head(), tail().toList())
//}

tailrec fun <ITEM:Any> goToList(stream :StreamF<ITEM>, list :ListF<ITEM>) :ListF<ITEM>
=when(stream) {
    is Empty ->list
    is Cons ->goToList(stream.tail(), ListF.cons(stream.head(), list))
}

/**
 * 연습문제 5.2
 * Stream의 맨 앞에서 원소를 n개 반환하는 take(n)과 맨 앞에서 원소를 n개 건너뛴 나머지 스트림을 돌려주는 drop(n)을 작성하라
 */
fun <ITEM:Any> StreamF<ITEM>.take(n :Int) :StreamF<ITEM>
=when(this){
    is Empty ->Empty
    is Cons ->if(n ==0) Empty else StreamF.cons({head()}) {tail().take(n -1)}
}

fun <ITEM:Any> StreamF<ITEM>.drop(n :Int) :StreamF<ITEM> =goDrop(n)
private fun <ITEM:Any> StreamF<ITEM>.goDrop(n :Int) :StreamF<ITEM>
=when(this) {
    is Empty ->Empty
    is Cons ->if(n ==0) this else tail().goDrop(n -1)
}

/**
 * 연습문제 5.3
 * 주어진 술어와 일치하는 모든 접두사(맨 앞부터 조건을 만족하는 연속된 원소들)를
 * 돌려주는 takeWhile을 작성하라.
 * REPL에서 스트림을 관찰하기 위해 take와 toList를 함께 사용할 수 있다.
 * 예를 들어 Stream.of(1,2,3).take(2).toList()를 출력해보라.
 * 단위 테스트에서 단언식을 작성할 때도 이 기법이 유용하다.
 */
fun <ITEM:Any> StreamF<ITEM>.takeWhile(block :(ITEM) ->Boolean) :StreamF<ITEM>
=when(this) {
    is Cons ->if(block(head())) StreamF.cons({head()}) {tail().takeWhile(block)}
        else Empty
    is Empty ->Empty
}

fun <ITEM:Any> StreamF<ITEM>.exists(block :(ITEM) ->Boolean) :Boolean
=when(this) {
    is Empty ->false
    is Cons ->block(head()) ||tail().exists(block)
}
fun <ITEM:Any, OTHER:Any> StreamF<ITEM>.foldRight(
    acc :() ->OTHER,
    block :(ITEM, () ->OTHER) ->OTHER
) :OTHER =when(this) {
    is Cons ->block(head()) {tail().foldRight(acc, block)}
    is Empty ->acc()
}
fun <ITEM:Any> StreamF<ITEM>.exists2(block :(ITEM) ->Boolean) :Boolean
=foldRight({false}) {a, b ->block(a) ||b()}

/**
 * 연습문제 5.4
 * Stream의 모든 원소가 주어진 술어를 만족하는지 검사하는 forAll을 구현하라.
 * 여러분의 구현은 술어를 만족하지 않는 값을 만나자마자 순회를 최대한 빨리 중단해야 한다.
 */
fun <ITEM:Any> StreamF<ITEM>.forAll(block :(ITEM) ->Boolean) :Boolean
=foldRight({true}) {a, b ->block(a) &&b()}
//=when(this) {
//    is Cons ->block(head()) &&tail().forAll(block)
//    is Empty ->true
//}

/**
 * 연습문제 5.5
 * foldRight를 사용해 takeWhile을 구현하라.
 */
fun <ITEM:Any> StreamF<ITEM>.takeWhileR(block :(ITEM) ->Boolean) :StreamF<ITEM>
=foldRight({ StreamF.empty() }) {it, acc ->
    if(block(it)) StreamF.cons({ it }, acc) else Empty
}

/**
 * 연습문제 5.6
 * 어려움: foldRight를 사용해 headOption을 구현하라
 */
fun <ITEM:Any> StreamF<ITEM>.headOptionR() :OptionF<ITEM>
=foldRight({OptionF.none()}) {it, _ -> OptionF.some(it)}

/**
 * 연습문제 5.7
 * foldRight를 사용해 map, filter, append를 구현하라.
 * append 메서드는 인자에 대해 엄격하지 않아야만 한다.
 * 필요하면 앞에서 정의한 함수를 사용할지 고려해보라.
 */
fun <ITEM:Any, OTHER:Any> StreamF<ITEM>.map(block :(ITEM) ->OTHER) :StreamF<OTHER>
=foldRight({StreamF.empty()}) {it, acc ->
    StreamF.cons({block(it)}, acc)
}
fun <ITEM:Any> StreamF<ITEM>.filter(block :(ITEM) ->Boolean) :StreamF<ITEM>
=foldRight({StreamF.empty()}) {it, acc ->
    if(block(it)) StreamF.cons({it}, acc) else acc()
}
fun <ITEM:Any> StreamF<ITEM>.append(block :() ->StreamF<ITEM>) :StreamF<ITEM>
=foldRight(block) {it, acc ->StreamF.cons({it}, acc)}


fun <ITEM:Any> StreamF<ITEM>.find(block :(ITEM) ->Boolean) :OptionF<ITEM> =filter(block).headOption()

/**
 * 연습문제 5.8
 * ones를 약간 일반화해서 정해진 값으로 이뤄진 무한 Stream을 돌려주는 constant함수를 작성 하라
 */
fun StreamF.Companion.ones() :StreamF<Int> =cons({ 1 }) { ones() }
fun <ITEM:Any> StreamF.Companion.constantA(v :ITEM) :StreamF<ITEM>
=cons({v}) {constantA(v)}
/**
 * 연습문제 5.9
 * n부터 시작해서 n +1, n +2 등을 차례로 내놓는 무한한 정수 스트림을 만들어내는 함수를 작성하라(
 * 코틀린에서 Int 타입은 32비트 부호가 있는 정수이므로 이 스트림은 약 40억 개의 정수를 주기적으로
 * 반복하면서 양수와 음수를 오간다).
 */
fun StreamF.Companion.fromA(n :Int) :StreamF<Int>
=cons({n}) {fromA(n +1)}

/**
 * 연습문제 5.10
 * 0,1,1,2,3,5,9 처럼 변하는 무한한 피보나치 수열을 만들어내는 fibs 함수를 작성하라.
 */
fun StreamF.Companion.fibsA() :StreamF<Int>
=goFibsA(0, 1)
fun StreamF.Companion.goFibsA(curr :Int, nxt :Int) :StreamF<Int>
=cons({curr}) { goFibsA(nxt, curr +nxt)}

/**
 * 연습문제 5.11
 * unfold라는 더 일반적인 스트림 구성 함수를 작성하라.
 * 이 함수는 초기 상태를 첫 번째 인자로 받고, 현재 상태로부터 다음 상태와 스트림상의 다음 값을 만들어내는
 * 함수를 두 번째 인자로 받는다.
 */
fun <ITEM:Any, STATE:Any> StreamF.Companion.unfold(
    state: STATE,
    block: (STATE) ->OptionF<Pair<ITEM, STATE>>
) :StreamF<ITEM>
=block(state).map {(item, state) ->
    cons({item}) {unfold(state, block)}
}.getOrElse {empty()}
//=when(val next =block(state)) {
//    is OptionF.Some ->cons({next.get.first}) {unfold(next.get.second, block)}
//    is OptionF.None ->empty()
//}

/**
 * 연습문제 5.12
 * unfold를 사용해 fibs, from, constant, ones를 구현하라.
 * 재귀적 버전에서 fun ones() :Stream<Int> =Stream.cons({1}, {ones()})로 공유를 사용했던 것과 달리
 * unfold를 사용해 constant와 ones를 정의하면 공유를 쓰지 않게 된다.
 * 재귀 정의는 순회를 하는 동안에도 스트림에 대한 참조를 유지하기 때문에 메모리를 상수로 소비하지만,
 * unfold 기반 구현은 그렇지 않다.
 * 공유 유지는 극히 미묘하며 타입을 통해 추적하기 어려우므로, 스트림을 사용해 프로그래밍을 할 때 일반적으로
 * 의존하는 특성은 아니다.
 * 예를 들어 단순히 xs.map {x ->x}를 호출해도 공유가 깨진다.
 */
fun StreamF.Companion.fibsUF() :StreamF<Int>
=unfold(0 to 1) {(first, second) ->OptionF.some(first to (second to first+second))}
fun StreamF.Companion.fromUF(n :Int) :StreamF<Int>
=unfold(n) {OptionF.some(it to (it +1))}
fun StreamF.Companion.constantUF(n :Int) :StreamF<Int>
=unfold(n) {OptionF.some(it to it)}
fun StreamF.Companion.onesUF() :StreamF<Int>
=unfold(1) {OptionF.some(it to it)}

/**
 * 연습문제 5.13
 * unfold를 사용해 map, take, takeWhile, zipWith(3장 참고), zipAll을 구현하라.
 * zipAll 함수는 두 스트림 중 한쪽에 원소가 남아 있는 한 순회를 계속해야 하며,
 * 각 스트림을 소진했는지 여부를 표현하기 위해 Option을 사용한다.
 */
fun <ITEM:Any, OTHER:Any> StreamF<ITEM>.mapUF(block :(ITEM) ->OTHER) :StreamF<OTHER>
=StreamF.unfold(this) {
    when(it) {
        is Cons ->OptionF.some(block(it.head()) to it.tail())
        is Empty ->OptionF.none()
    }
}
fun <ITEM:Any> StreamF<ITEM>.takeUF(n :Int) :StreamF<ITEM>
=StreamF.unfold(this) {
    when(it) {
        is Cons ->if(n ==0) OptionF.none()
            else OptionF.some(it.head() to it.tail().takeUF(n -1))
        else ->OptionF.none()
    }
}
fun <ITEM:Any> StreamF<ITEM>.takeWhileUF(block :(ITEM) ->Boolean) :StreamF<ITEM>
=StreamF.unfold(this) {
    if(it is Cons) {
        if(block(it.head())) OptionF.some(it.head() to it.tail())
        else OptionF.none()
    } else OptionF.none()
}
fun <ITEM:Any, OTHER:Any, RESULT:Any> StreamF<ITEM>.zipWithUF(
    that :StreamF<OTHER>,
    block :(ITEM, OTHER) ->RESULT
) :StreamF<RESULT>
=StreamF.unfold(this to that) {(a, b) ->
    if(a is Cons && b is Cons) {
        OptionF.some(block(a.head(), b.head()) to Pair(a.tail(), b.tail()))
    } else OptionF.none()
//    when(a) {
//        is Cons ->when(b) {
//            is Cons ->OptionF.some(block(a.head(), b.head()) to (a.tail() to b.tail()))
//            is Empty ->OptionF.none()
//        }
//        is Empty ->OptionF.none()
//    }
}
fun <ITEM:Any, OTHER:Any> StreamF<ITEM>.zipAllUF(
    that :StreamF<OTHER>
) :StreamF<Pair<OptionF<ITEM>, OptionF<OTHER>>>
=StreamF.unfold(this to that) {(a, b) ->
    if(a is Cons) {
        when(b) {
            is Cons ->OptionF.some(Pair(
                OptionF.some(a.head()) to OptionF.some(b.head()),
                a.tail() to b.tail()
            ))
            else ->OptionF.some(Pair(
                OptionF.some(a.head()) to OptionF.none(),
                a.tail() to StreamF.empty()
            ))
        }
    } else {
        when(b) {
            is Cons ->OptionF.some(Pair(
                OptionF.none<ITEM>() to OptionF.some(b.head()),
                StreamF.empty<ITEM>() to b.tail()
            ))
            else ->OptionF.none()
        }
    }
}

/**
 * 연습문제 5.14
 * 어려움: 이전에 작성한 함수를 사용해 startWith를 구현하라.
 * 이 함수는 어떤 Stream이 다른 Stream의 접두사인지 여부를 검사해야 한다.
 * 예를 들어 Stream(1,2,3).startsWith(Stream(1,2))는 true다.
 * 팁: 이 문제는 이번 장의 앞부분에서 unfold를 사용해 개발한 함수만을 사용해 구현할 수 있다.
 */
fun <ITEM:Any> StreamF<ITEM>.startWith(that :StreamF<ITEM>) :Boolean
=zipAllUF(that)
    .takeWhileUF {
        it.second !=OptionF.none<ITEM>()
    }
    .forAll {
        it.first ==it.second
    }

/**
 * 연습문제 5.15
 * unfold를 사용해 tails를 구현하라.
 * tails는 주어진 Stream의 모든 접미사를 돌려준다.
 * 이때 원래 Stream과 똑같은 스트림을 가장 먼저 돌려준다.
 * 예를 들어 Stream.of(1,2,3)에 대해 tails는 Stream.of(Stream.of(1,2,3), Stream.of(2,3), Stream.of(3),
 * Stream.empty())를 반환 한다.
 */
fun <ITEM:Any> StreamF<ITEM>.tails() :StreamF<StreamF<ITEM>>
=StreamF.unfold(this) {
    when(it) {
        is Cons ->OptionF.some(it to it.tail())
        else ->OptionF.none()
    }
}

/**
 * 연습문제 5.16
 * 어려움/선택적: tails를 일반화해서 scanRight를 만들라.
 * scanRight는 foldRight와 마찬가지로 중간 결과로 이뤄진 스트림을 반환한다.
 * 예를 들면 다음과 같다.
 * >>> Stream.of(1,2,3).scanRight(0, {a, b ->a +b}).toList()
 * res1: ListF<Int>
 * =Cons(head=6, tail=Cons(head=5, tail=Cons(head=3, tail=Cons(head=0, tail=Nil))))
 * 이 예제는 List.of(1+2+3+0, 2+3+0, 3+0, 0)이라는 식과 같다.
 * 여러분의 함수는 중간 결과를 재사용해서 원소가 n개인 Stream을 순회하는 데 걸린 시간이 n에 선형적으로 비례해야 만 한다.
 * unfold를 사용해 이 함수를 구현할 수 있을까?
 * 구현할 수 있다면 어떻게 구현할 수 있고, 구현할 수 없다면 왜 구현할 수 없을까?
 * 여러분이 지금까지 작성한 다른 함수를 사용해 이 함수를 구현할 수 있을까?
 */
fun <ITEM:Any, OTHER:Any> StreamF<ITEM>.scanRight(
    other :OTHER,
    block :(ITEM, () ->OTHER) ->OTHER) :StreamF<OTHER>
//=StreamF.unfold(this) {
//    when(it) {
//        is Cons ->OptionF.some(block(it.head()) {it.tail().foldRight({other}, block)} to it.tail())
//        else ->OptionF.none()
//    }
//}
=foldRight({other to StreamF.of(other)}, {item, p0 ->
    val p1 :Pair<OTHER, StreamF<OTHER>> by lazy { p0() }
    val b2 :OTHER =block(item) {p1.first}
    Pair(b2, StreamF.cons({b2}) {p1.second})
}).second




// ===== 테스트 =====
package khs

import arrow.core.const
import kotlin.test.Test
import kotlin.test.assertEquals

class Chapter5Test {
    @Test
    fun streamTest() {
        val stream =StreamF.of(1,2,3,4)

        println("=====> stream: $stream")
        assertEquals(stream.toList(), ListF.of(1,2,3,4))
        assertEquals(stream.take(2).toList(), ListF.of(1,2))
        assertEquals(stream.drop(2).toList(), ListF.of(3,4))
        assertEquals(stream.takeWhile { it <=2 }.toList(), ListF.of(1,2))
        assertEquals(stream.forAll { it <=4 }, true)
        assertEquals(stream.forAll { it <=2 }, false)
        assertEquals(stream.takeWhileR { it <=2 }.toList(), ListF.of(1,2))
        assertEquals(stream.headOptionR(), OptionF.some(1))
        assertEquals(stream.map { it+1 }.toList(), ListF.of(2,3,4,5))
        assertEquals(stream.filter { it <=2 }.toList(), ListF.of(1,2))
        assertEquals(stream.append { StreamF.of(10,20) }.toList(), ListF.of(1,2,3,4,10,20))
        assertEquals(StreamF.ones().take(2).toList(), ListF.of(1,1))
        assertEquals(StreamF.constantA(1).take(2).toList(), ListF.of(1,1))
        assertEquals(StreamF.fromA(2).take(2).toList(), ListF.of(2,3))
        assertEquals(StreamF.fibsA().take(5).toList(), ListF.of(0,1,1,2,3))
        assertEquals(StreamF.unfold(0) { OptionF.some(Pair(it, it+1)) }.take(5).toList(), ListF.of(0,1,2,3,4))
        assertEquals(StreamF.onesUF().take(2).toList(), ListF.of(1,1))
        assertEquals(StreamF.fibsUF().take(5).toList(), ListF.of(0,1,1,2,3))
        assertEquals(StreamF.fromUF(2).take(2).toList(), ListF.of(2,3))
        assertEquals(StreamF.constantUF(1).take(2).toList(), ListF.of(1,1))
        assertEquals(stream.mapUF {"$it"}.toList(), ListF.of("1","2","3","4"))
        assertEquals(stream.takeUF(2).toList(), ListF.of(1,2))
        assertEquals(stream.takeWhileUF {it <=2}.toList(), ListF.of(1,2))
        assertEquals(
            stream.zipWithUF(StreamF.of(10,20,30,40)) {a, b ->a +b}.toList(),
            ListF.of(11,22,33,44)
        )
        assertEquals(
            stream.zipAllUF(StreamF.of(10,20,30)).toList(),
            ListF.of(
                OptionF.some(1) to OptionF.some(10),
                OptionF.some(2) to OptionF.some(20),
                OptionF.some(3) to OptionF.some(30),
                OptionF.some(4) to OptionF.none()
            )
        )
        assertEquals(stream.startWith(StreamF.of(1,2)), true)
        assertEquals(stream.startWith(StreamF.of(1,2,4)), false)
        assertEquals(
            stream.tails().toList().map {it.toList()},
            ListF.of(
                ListF.of(1,2,3,4),
                ListF.of(2,3,4),
                ListF.of(3,4),
                ListF.of(4),
            )
        )
        assertEquals(
            stream.scanRight(0) {a, b ->a +b()}.toList(),
            ListF.of(10,9,7,4,0)
        )
    }
}