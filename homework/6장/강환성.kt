package khs

import arrow.core.const

interface RNG {
    fun nextInt() :Pair<Int, RNG>
}
data class SimpleRNG(val seed :Long) :RNG{
    override fun nextInt(): Pair<Int, SimpleRNG> {
        val newSeed =(seed * 0x5DEECE66DL + 0xBL) and 0xFFFFFFFFFFFFL
        val nextRNG =SimpleRNG(newSeed)
        val n =(newSeed ushr 16).toInt()
        return n to nextRNG
    }
    companion object
}

/**
 * 연습문제 6.1
 * RNG.nextInt를 사용해 0이상 Int.MAX_VALUE 이하의 난수 정수를 생성하는 함수를 작성하라.
 * 팁: 각 음수를 서로 다른 양수로 매핑해야 한다.
 * nextInt가 Int.MIN_VALUE를 반환하는 경우(이렇게 정상에 속하기는 하지만 아주 극단적인 경우를 코너케이스라고 함),
 * 이 값에 대응하는 양수가 없으므로 이를 감안해 처리해야 한다.
 */
fun SimpleRNG.nonNegativeInt() :Pair<Int, SimpleRNG> {
    val (i, rng2) =nextInt()
    return (if(i <0) -(i +1) else i) to rng2
}

/**
 * 연습문제 6.2
 * 0이상 1미만(1을 포함하지 않는다는 데 주의하라)의 Double을 생성하는 함수를 작성하라.
 * 여러분이 이미 개발한 함수와 더불어 최대 정수 값을 얻는 Int.MAX_VALUE와 Int 타입의 x를
 * Double로 변환하는 x.toDouble()을 사용할 수 있다.
 */
fun SimpleRNG.double() :Pair<Double, SimpleRNG> =nonNegativeInt().let {(i, rng) ->
    i / (Int.MAX_VALUE.toDouble() +1) to rng
}

/**
 * 연습문제 6.3
 * Pair<Int, Double>, Pair<Double, Int>, Trible<Double, Double, Double>을 생성하는 함수를 만들라.
 * 여러분이 이미 작성한 함수를 재사용할 수 있어야 한다.
 */
fun SimpleRNG.intDouble() :Pair<Pair<Int, Double>, SimpleRNG>
=nextInt().let {
    val (d, rng2) =it.second.double()
    return Pair(it.first, d) to rng2
}
fun SimpleRNG.doubleInt() :Pair<Pair<Double, Int>, SimpleRNG>
=intDouble().let {(id, rng2) ->
    Pair(id.second, id.first) to rng2
}
fun SimpleRNG.double3() :Pair<Triple<Double, Double, Double>, SimpleRNG>
=double().let {
    val (d1, rng1) =it
    val (d2, rng2) =rng1.double()
    val (d3, rng3) =rng2.double()
    return Triple(d1, d2, d3) to rng3
}
/**
 * 연습문제 6.4
 * 난수 정수의 리스트를 생성하는 함수를 작성하라.
 */
fun SimpleRNG.ints(n :Int) :Pair<ListF<Int>, SimpleRNG>
=nextInt().let {(i, rng) ->
    if(n ==0) ListF.empty<Int>() to rng
    else {
        val (list, rng2) =rng.ints(n -1)
        ListF.of(i).append(list) to rng2
    }
}

/**
 * 연습문제 6.5
 */
typealias RandF<ITEM> =(SimpleRNG) ->Pair<ITEM, SimpleRNG>
fun <ITEM:Any> SimpleRNG.Companion.unit(i :ITEM) :RandF<ITEM> ={i to it}
fun <ITEM:Any, OTHER:Any> SimpleRNG.Companion.map(
    f :RandF<ITEM>,
    block :(ITEM) ->OTHER
) :RandF<OTHER> ={
    val (a, rng2) =f(it)
    block(a) to rng2
}
fun SimpleRNG.Companion.nonNegativeEven() :RandF<Int>
=map({it.nonNegativeInt()}) {it -(it %2)}
fun SimpleRNG.Companion.doubleR() :RandF<Double>
=map({it.double()}) {it}

/**
 * 연습문제 6.6
 * 다음 시그니처에 맞춰 map2 구현을 작성하라.
 * 이 함수는 ra와 rb라는 두 동작과 이 두 동작의 결과를 조합하는 f라는 함수를 받아서
 * 두 동작의 결과를 조합한 새 동작을 반환한다.
 */
fun <ITEM1:Any, ITEM2:Any, OTHER:Any> SimpleRNG.Companion.map2(
    ra :RandF<ITEM1>,
    rb :RandF<ITEM2>,
    block :(ITEM1, ITEM2) ->OTHER
) :RandF<OTHER> ={
    val (a, r1) =ra(it)
    val (b, r2) =rb(r1)
    block(a, b) to r2
}
fun <ITEM1:Any, ITEM2:Any> SimpleRNG.Companion.both(
    ra :RandF<ITEM1>,
    rb :RandF<ITEM2>
) :RandF<Pair<ITEM1, ITEM2>>
=map2(ra, rb) {a, b ->Pair(a, b)}
/**
 * 연습문제 6.7
 * 어려움: 여러분이 두 RNG 전이를 조합할 수 있다면, RNG로 이뤄지 리스트를 조합할 수도 있어야 한다.
 * 전이의 List를 단일 전이로 조합하는 sequence를 구현하라.
 * 이 구현을 사용해 연습문제 6.4에서 작성했던 ints 함수를 다시 구현하라.
 * 이 문제를 단순화하고자, x를 n번 반복한 리스트를 생성하는 ints를 재귀를 사용해 구현한 것도 정답으로 인정할 수 있다.
 * sequence를 구현하고 나서 이를 fold를 사용해 재구현 해보라.
 */
fun <ITEM:Any> SimpleRNG.Companion.sequence1(fs :ListF<RandF<ITEM>>) :RandF<ListF<ITEM>>
={when(fs) {
    is ListF.Cons ->{
        val (a, rng) =fs.head(it)
        val (xa, nxtRng) =sequence1(fs.tail)(rng)
        ListF.of(a).append(xa) to nxtRng
    }
    is ListF.Nil ->Pair(ListF.empty(), it)
}}
fun <ITEM:Any> SimpleRNG.Companion.sequence2(fs :ListF<RandF<ITEM>>) :RandF<ListF<ITEM>>
=fs.foldRight({ListF.empty<ITEM>() to it}) {rng, acc ->
    {
        val (a, rng2) =rng(it)
        val (xa, nxtRng) =acc(rng2)
        ListF.of(a).append(xa) to nxtRng
    }
}
fun SimpleRNG.ints2(n :Int) :Pair<ListF<Int>, SimpleRNG> {
    fun go(c :Int) :ListF<RandF<Int>>
    =if(c ==0) ListF.empty()
    else ListF.of<RandF<Int>>({it.nextInt()}).append(go(c -1))

    return SimpleRNG.sequence2(go(n))(this)
}
/**
 * 연습문제 6.8
 * flatMap을 구현하고 이를 사용해 nonNegativeLessThan을 구현하라.
 */
fun <ITEM:Any, OTHER:Any> SimpleRNG.Companion.flatMap(
    f :RandF<ITEM>,
    block :(ITEM) ->RandF<OTHER>
) :RandF<OTHER>
={
    val (a, rng) =f(it)
    block(a)(rng)
}
fun SimpleRNG.Companion.nonNegativeLessThan(n :Int) :RandF<Int>
=flatMap({it.nonNegativeInt()}) {
    val mod =it %n
    if(it +(n -1) -mod >=0) unit(mod) else nonNegativeLessThan(n)
}
/**
 * 연습문제 6.9
 * map과 map2를 flatMap을 활용해 재구현하라.
 * map이나 map2보다 flatMap이 더 강력하다는 이야기를 할 때 이런 재구현이 가능하다는 사실을 언급할 수 있다.
 */
fun <ITEM:Any, OTHER:Any> SimpleRNG.Companion.mapF(
    f :RandF<ITEM>,
    block :(ITEM) ->OTHER
) :RandF<OTHER>
=flatMap(f) {unit(block(it))}
fun SimpleRNG.Companion.nonNegativeEvenF() :RandF<Int>
=mapF({it.nonNegativeInt()}) {it -(it %2)}
fun SimpleRNG.Companion.doubleRF() :RandF<Double>
=mapF({it.double()}) {it}
fun <ITEM1:Any, ITEM2:Any, OTHER:Any> SimpleRNG.Companion.map2F(
    ra :RandF<ITEM1>,
    rb :RandF<ITEM2>,
    block :(ITEM1, ITEM2) ->OTHER
) :RandF<OTHER>
=flatMap(ra) {a ->
    map(rb) {block(a, it)}
}

/**
 * 연습문제 6.10
 * unit, map, map2, flatMap, sequence함수를 일반화 하라.
 * 이때 State 데이터 클래스의 메서드로 추가할 수 있다면 메서드로 추가하라.
 * 클래스에 추가하는 대신 동반 객체에 추가하는 편이 더 적절하다면 그렇게 해도 좋다.
 * 항상 메서드가 동반 객체나 데이터 타입 중 어디에 있어야 할지 심사숙고하라.
 * map처럼 데이터 타입의 인스턴스에 대해 작용하는 메서드인 경우,
 * 분명 이를 클래스 수준에 놓는 게 적절하다.
 * unit 메서드처럼 값을 만들어내는 경우나 map2나 sequence처럼 여러 인스턴스에 대해
 * 작용하는 메서드인 경우 이들을 동반 객체에 넣는 게 더 적합할 것이다.
 * 이런 선택은 종종 개인의 취향에 따라 달라질 수 있고,
 * 구현을 누가 제공하느냐에 따라 달라질 수 있다.
 */
typealias StateT<STATE, ITEM> =(STATE) ->Pair<ITEM, STATE>
typealias StateRand<ITEM> =StateF<RNG, ITEM>
data class StateF<STATE:Any, out VALUE:Any>(val run :(STATE) ->Pair<VALUE, STATE>) {
    companion object

    fun <OTHER:Any> flatMap(block :(VALUE) ->StateF<STATE, OTHER>) :StateF<STATE, OTHER>
    = StateF {
        val (v, s) =this.run(it)
        block(v).run(s)
    }
    fun <OTHER:Any> map(block :(VALUE) ->OTHER) :StateF<STATE, OTHER>
    =flatMap {unit(block(it))}
}
fun <STATE:Any, VALUE:Any> StateF.Companion.unit(v :VALUE) :StateF<STATE, VALUE>
= StateF {v to it}
fun <STATE:Any, VALUE1:Any, VALUE2:Any, OTHER:Any> StateF.Companion.map2(
    sa :StateF<STATE, VALUE1>,
    sb :StateF<STATE, VALUE2>,
    block :(VALUE1, VALUE2) ->OTHER
) :StateF<STATE, OTHER>
=sa.flatMap {v ->
    sb.map {block(v, it)}
}
fun <STATE:Any, VALUE:Any> StateF.Companion.sequence(
    fs :ListF<StateF<STATE, VALUE>>
) :StateF<STATE, ListF<VALUE>>
=fs.foldRight(unit(ListF.empty())) {it, acc ->
    map2(it, acc) {a, b -> ListF.of(a).append(b)}
}

/**
 * 연습문제 6.11
 * 어려움/선택적: State 사용 경험을 얻기 위해, 간단한 캔디 자판기를 모델링하는 유한 상태 오토마톤을 구현하자.
 * 이 자판기에는 두 가지 입력이 존재한다.
 * 즉, 동전을 집어 넣을 수 도 있고 캔디를 빼내기 위해 동그란 손잡이를 돌리 수도 있다.
 * 자판기의 상태는 잠겨 있거나 잠겨 있지 않는 등 두 가지다.
 * 그리고 캔디가 몇 개 남았는지와 동전이 몇개 들어 있는지를 추적한다.
 *
 * 이 자판기의 규칙은 다음과 같다.
 * - 잠긴 자판기에 동전을 넣었는데, 남은 캔디가 있으면 자판기 잠금이 풀린다.
 * - 잠금이 풀린 자판기에서 손잡이를 돌리면 캔디가 나오고 잠긴 상태로 바뀐다.
 * - 잠긴 자판기에서 손잡이를 돌리거나, 잠금이 풀린 자판기에서 동전을 넣으면 아무 일도 벌어지지 않는다.
 * - 캔디가 떨어진 자판기는 모든 입력을 무시한다.
 */
sealed class InputF
data object Coin :InputF()
data object Turn :InputF()
data class Machine(val locked :Boolean, val candies :Int, val coins :Int) {
    companion object {
        val update :(InputF) ->(Machine) ->Machine ={input ->
            {machine ->when(input) {
                is Coin ->{
                    if(!machine.locked || machine.candies ==0) machine
                    else Machine(false, machine.candies, machine.coins +1)
                }
                is Turn ->{
                    if(machine.locked ||machine.candies ==0) machine
                    else Machine(true, machine.candies -1, machine.coins)
                }
            }}
        }
        fun simulateMachine(inputs :ListF<InputF>) :StateF<Machine, Pair<Int, Int>>
        =StateF {machine ->
            if(inputs is ListF.Cons) {
                simulateMachine(inputs.tail)
                    .run(update(inputs.head)(machine))
            } else machine.coins to machine.candies to machine
        }
    }
}



// ===== 테스트 =====
package khs

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class Chapter6Test {
    @Test
    fun test1() {
        val rng =SimpleRNG(42)
        val (n1, rng2) =rng.nextInt()
        println("n1: $n1, rng2: $rng2")
        val (n2, rng3) =rng2.nextInt()
        println("n2: $n2, rng3: $rng3")

        assertEquals(rng.nonNegativeInt().first, n1)
        assertEquals(rng.double().first, n1 / (Int.MAX_VALUE.toDouble() +1))
        assertEquals(rng.intDouble().first, Pair(16159453, 0.5967354848980904))
        assertEquals(rng.doubleInt().first, Pair(0.5967354848980904, 16159453))
        assertEquals(rng.double3().first, Triple(0.007524831686168909, 0.5967354848980904, 0.15846728393808007))
        assertEquals(rng.ints(3).first, ListF.of(16159453, -1281479697, -340305902))

        assertEquals(SimpleRNG.nonNegativeEven()(rng).first, 16159452)
        assertEquals(SimpleRNG.doubleR()(rng).first, 0.007524831686168909)
        assertEquals(SimpleRNG.map2({it.nextInt()}, {it.nextInt()}, {a, b ->a +b})(rng).first, 16159453 + -1281479697)

        val intR :RandF<Int> ={it.nextInt()}
        val doubleR :RandF<Double> ={it.double()}
        val intDoubleR :RandF<Pair<Int, Double>> =SimpleRNG.both(intR, doubleR)
        val doubleIntR :RandF<Pair<Double, Int>> =SimpleRNG.both(doubleR, intR)
        assertEquals(intDoubleR(rng).first, Pair(16159453, 0.5967354848980904))
        assertEquals(doubleIntR(rng).first, Pair(0.007524831686168909, -1281479697))
        assertEquals(rng.ints2(3).first, ListF.of(16159453, -1281479697, -340305902))
        assertEquals(SimpleRNG.nonNegativeLessThan(41)(rng).first, 0)
        assertEquals(SimpleRNG.nonNegativeEvenF()(rng).first, 16159452)
        assertEquals(SimpleRNG.doubleRF()(rng).first, 0.007524831686168909)
        assertEquals(
            SimpleRNG.map2F({it.nextInt()}, {it.nextInt()}, {a, b ->a +b})(rng).first,
            16159453 + -1281479697
        )

        val rollDie :RandF<Int> =SimpleRNG.nonNegativeLessThan(6)
        println(rollDie(SimpleRNG(5)).first)
    }

    @Test
    fun test2() {
        val state =StateF.unit<Int, Int>(1)
        println("state=====> " +state.run(2))

        assertEquals(state.run(2), 1 to 2)
        assertEquals(state.map {"$it"}.run(2), "1" to 2)
        assertEquals(StateF.map2(state, state) {a, b ->"${a +b}"}.run(2), "2" to 2)
        assertEquals(StateF.sequence(ListF.of(state, state, state)).run(2), ListF.of(1, 1, 1) to 2)
    }

    @Test
    fun test3() {
        val (v, machine) =Machine.simulateMachine(ListF.of(
            Coin,
            Turn,
            Coin,
            Turn
        )).run(Machine(true, 5, 0))
        val (coin, candy) =v
        println("machine=====> $machine")
        assertEquals(coin, 2)
        assertEquals(candy, 3)
        assertEquals(machine, Machine(true, 3, 2))
    }
}