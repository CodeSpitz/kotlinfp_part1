// ===== 연습문제2 =====
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


// ========== 3장 연습문제 ==========

import khs.ListF.Nil
import khs.ListF.Cons
sealed class ListF<out ITEM: Any> {
    companion object {
        fun <ITEM: Any> of(vararg aa: ITEM): ListF<ITEM> {
            val tail = aa.sliceArray(1..< aa.size)
            return if(aa.isEmpty()) Nil else Cons(aa[0], of(*tail))
        }
        fun <ITEM: Any> empty():ListF<ITEM> = Nil
        fun <ITEM:Any> cons(head :ITEM, tail :ListF<ITEM>): ListF<ITEM> =Cons(head, tail)
    }
    data object Nil: ListF<Nothing>()
    data class Cons<out ITEM: Any>(val head: ITEM, val tail: ListF<ITEM>): ListF<ITEM>()
}

/**
 * 연습문제 3.1
 * List의 첫 번째 원소를 제거하는 tail 함수를 구현하라.
 * 이 함수는 상수 시산에 실행이 끝나야 한다.
 * List가 Nil일 때 선택할 수 있는 여러 가지 처리 방법을 생각해보라.
 * 다음 장에서 이 경우(함수가 정상 작동하지 못하는 경우)를 다시 살펴본다.
 */
fun <ITEM: Any> ListF<ITEM>.tail(): ListF<ITEM>
= when(this) {
    is Nil-> Nil
    is Cons-> tail
}

/**
 * 연습문제 3.2
 * 연습문제 3.1과 같은 아이디어를 사용해 List의 첫 원소를 다른 값으로 대치하는 setHead함수를 작성하라.
 */
fun <ITEM: Any> ListF<ITEM>.setHead(item: ITEM): ListF<ITEM>
= when(this) {
    is Nil-> Nil
    is Cons-> Cons(item, tail)
}

/**
 * 연습문제 3.3
 * tail을 더 일반화해서 drop 함수를 작성하라.
 * drop은 리스트 맨 앞부터 n개 원소를 제거한다.
 * 이 함수는 삭제할 원소의 개수에 비례해 시간이 걸리다는
 * 사실(따라서 여러분이 전체 List를 복사할 필요가 없다는 사실)을 알아두라.
 */
fun <ITEM: Any> ListF<ITEM>.drop(n: Int): ListF<ITEM>
= when(this) {
    is Nil-> Nil
    is Cons-> goDrop(this, n)
}
private tailrec fun <ITEM: Any> goDrop(list: ListF<ITEM>, n: Int): ListF<ITEM>
= if(n==0) list else goDrop(list.tail(), n-1)

/**
 * 연습문제 3.4
 * dropWhile을 구현하라. 이 함수는 List의 맨 앞에서 부터 주어진 술어를
 * 만족(술어 함수가 true를 반환)하는 연속적인 원소를 삭제한다.
 * (다른 말로 하면, 이 함수는 주어진 술어를 만족하는 접두사를 List에서 제거한다.)
 */
fun <ITEM: Any> ListF<ITEM>.dropWhile(block: (ITEM)-> Boolean): ListF<ITEM>
= when(this) {
    is Nil-> Nil
    is Cons-> {
        if(block(head)) tail.dropWhile(block) else this
    }
}

/**
 * 연습문제 3.5
 * 코드가 리스트를 연결하는 코드의 경우처럼 항상 제대로 작동하는 것은 아니다.
 * 어떤 List에서 마지막 원소를 제외한 나머지 모든 원소로 이뤄진(순서는 동일한)
 * 새 List를 반환하는 init 함수를 정의하라.
 * 예를 들어 List(1,2,3,4)에 대해 init은 List(1,2,3)을 돌려줘야 한다.
 * 이 함수를 tail처럼 상수 시간에 구현할 수 없는 이유는 무엇일까?
 */
fun <ITEM: Any> ListF<ITEM>.init(): ListF<ITEM>
= when(this) {
    is Cons-> if(tail == Nil) Nil else Cons(head, tail.init())
    is Nil-> Nil
}
// tail이 Nil일때 까지 순회 해야 한다.

/**
 * 연습문제 3.6
 * foldRigth로 구현된 product가 리스트 원소로 0.0을 만나면 재귀를 즉시 중단하고 결과를 돌려줄 수 있는가?
 * 즉시 결과를 돌려줄 수 있거나 돌려줄 수 없는 이유는 무엇인가?
 * 긴 리스트에 대해 쇼트 서킷을 제공할 수 있으면 어떤 장점이 있을지 생각해보라.
 * 5장에서는 이 질문이 내포하고 있는 의미를 더 자세히 살펴본다.
 */
// 리스트 3.10 쇼트 서킷을 제거함으로써 product 정규화 하기
fun ListF<Int>.sum(): Int = when(this) {
    is Cons-> head + tail.sum()
    is Nil-> 0
}
fun ListF<Double>.product(): Double = when(this) {
    is Cons-> head * tail.product()
    is Nil-> 1.0
}
// 리스트 3.11 foldRight를 사용해 product와 sum 일반화하기
fun <ITEM:Any, OTHER:Any> ListF<ITEM>.foldRight(acc:OTHER, block:(it:ITEM, acc:OTHER)-> OTHER): OTHER
= when(this) {
    is Cons-> block(head, tail.foldRight(acc, block))
    is Nil-> acc
}
fun ListF<Int>.sumFoldRight(): Int = foldRight(0) { it, acc -> it + acc }
fun ListF<Double>.productFoldRight(): Double = foldRight(1.0) { it, acc -> it * acc }

/**
 * 연습문제 3.7
 * 다음과 같이 Nil과 Cons를 foldRight에 넘길 때 각가 어떤 일이 벌어지는지 살펴보라.
 * (여기서는 Nil as List<Int>라고 타입을 명시해야 한다.
 * 그렇지 않으면 코틀린이 foldRight의 B 타입 파라미터를 List<Nothing>으로 추론한다.)
 */
fun ex0307(list:ListF<Int>): ListF<Int>
= list.foldRight(Nil as ListF<Int>) {it, acc -> Cons(it, acc)}
//fun <ITEM: Any> empty(): ListF<ITEM> = Nil

/**
 * 연습문제 3.8
 * foldRight를 사용해 리스트 길이를 계산하라.
 */
fun <ITEM:Any> ListF<ITEM>.length(): Int = foldRight(0) {_, acc -> acc+1}

/**
 * 연습문제 3.9
 * 우리가 구현한 foldRight는 꼬리 재귀가 아니므로 리스트가 긴 경우 StackOverflowError를 발생시킨다
 * (이를 스택 안전 하지 않다고 말한다). 정말 우리 구현이 스택 안전하지 않은지 확인하라.
 * 그 후 다른 리스트 재귀 함수 foldLeft를 2장에서 설명한 기법을 사용해 꼬리 재귀로 작성하라.
 * 다음은 foldLeft의 시그니처다.
 */
fun <ITEM:Any, OTHER:Any> ListF<ITEM>.foldLeft(acc:OTHER, block:(OTHER, ITEM)-> OTHER): OTHER
= when(this) {
    is Cons-> tail.foldLeft(block(acc, head), block)
    is Nil-> acc
}

/**
 * 연습문제 3.10
 * foldLeft를 사용해 sum, product 리스트 길이 계산 함수를 작성하라.
 */
fun ListF<Int>.sumFoldLeft():Int = foldLeft(0) {acc, it -> acc + it}
fun ListF<Double>.productFoldLeft():Double = foldLeft(1.0) {acc, it -> acc * it}
fun <ITEM:Any> ListF<ITEM>.lengthFoldLeft():Int = foldLeft(0) {acc, _ -> acc + 1}

/**
 * 연습문제 3.11
 * 리스트 순서를 뒤집은 새 리스트를 반환하는 함수(List(1,2,3)이 주어지면 List(3,2,1)을 반환)를 작성하라.
 * 이 함수를 접기 연산(foldRight와 foldLeft를 합쳐서 접기 연산이라고 한다)을 사용해 작성할 수 있는지 살펴보라.
 */
fun <ITEM:Any> ListF<ITEM>.reverse():ListF<ITEM> = foldLeft(ListF.empty()) { acc, it -> Cons(it, acc)}

/**
 * 연습문제 3.12
 * foldLeft를 foldRight를 사용해 작성할 수 있는가?
 * 반대로 foldRight를 foldLeft를 사용해 작성할 수 있는가?
 * foldRight를 foldLeft로 구현하면 foldRight를 꼬리 재귀로 구현 할 수 있기 때문에 유용하다.
 * 이 말은 큰 리스트를 스택 오버플로를 일으키지 않고 foldRight로 처리할 수 있다는 뜻이다.
 */

fun <ITEM:Any, OTHER:Any> ListF<ITEM>.foldLeftR(other:OTHER, block:(OTHER, ITEM)-> OTHER):OTHER
= reverse().foldRight(other) { it, acc -> block(acc, it)}
fun <ITEM:Any, OTHER:Any> ListF<ITEM>.foldRightL(other:OTHER, block:(ITEM, OTHER)-> OTHER):OTHER
= reverse().foldLeft(other) {acc, it -> block(it, acc)}

/**
 * 연습문제 3.13
 * append를 foldLeft나 foldRight를 사용해 구현하라.
 */
fun <ITEM:Any> ListF<ITEM>.append(list:ListF<ITEM>):ListF<ITEM>
= when(this) {
    is Cons-> Cons(head, tail.append(list))
    is Nil-> list
}

/**
 * 연습문제 3.14
 * 리스트가 원소인 리스트를 단일 리스트로 연결해주는 함수를 작성하라.
 * 이 함수의 실행 시간은 모든 리스트의 길이 합계에 선형으로 비례해야 한다.
 * 이 책에서 지금까지 정의한 함수를 활용하라.
 */
fun <ITEM:Any> ListF<ListF<ITEM>>.flatten():ListF<ITEM>
= foldRight(ListF.empty()) {it, acc ->
    it.foldRight(acc) {it2, acc2 -> Cons(it2, acc2)}
}

/**
 * 연습문제 3.15
 * 정수로 이뤄진 리스트를 각 원소에 1을 더한 리스트로 변환하는 함수를 작성하라.
 * 이 함수는 순수 함수이면서 새 List를 반환해야 한다.
 */
fun ListF<Int>.intPlusOne(): ListF<Int>
= foldRight(ListF.empty()) {it, acc -> Cons(it+1, acc)}
//= when(this) {
//    is Cons-> Cons(head+1, tail.intPlusOne())
//    is Nil-> Nil
//}

/**
 * 연습문제 3.16
 * List<Double>의 각 원소를 String으로 변환하는 함수를 작성하라.
 * d.toString()을 사용하면 Double 타입인 d를 String으로 바꿀 수 있다.
 */
fun ListF<Double>.doubleToString(): ListF<String>
= foldRight(ListF.empty()) {it, acc -> Cons(it.toString(), acc)}
//= when(this) {
//    is Cons-> Cons(head.toString(), tail.doubleToString())
//    is Nil-> Nil
//}

/**
 * 연습문제 3.17
 * 리스트의 모든 원소를 변경하되 리스트 구조는 그대로 유지하는 map 함수를 작성하라.
 * 다음은 map의 시그니처다.(표준 라이브러리에서 map과 flatMap은 List의 메서드다.)
 */
fun <ITEM:Any, OTHER:Any> ListF<ITEM>.map(block:(ITEM)-> OTHER): ListF<OTHER>
= foldRight(ListF.empty()) {it, acc -> Cons(block(it), acc)}

/**
 * 연습문제 3.18
 * 리스트에 주어진 술어를 만족하지 않는 원소를 제거해주는 filter 함수를 작성하라.
 * 이 함수를 사용해 List<Int>에서 홀수를 모두 제거하라.
 */
fun <ITEM:Any> ListF<ITEM>.filter(block:(ITEM)-> Boolean): ListF<ITEM>
= foldRight(ListF.empty()) {it, acc -> if(block(it)) Cons(it, acc) else acc}

/**
 * 연습문제 3.19
 * map처럼 작동하지만 인자로(단일 값이 아니라) 리스트를 반환하는 함수를 받는 flatMap 함수를 작성하라.
 * 이때 인자로 전달된 함수가 만들어낸 모든 리스트의 원소가 (순서대로) 최종 리스트 안에 삽입돼야 한다.
 * 다음은 flatMap의 시그니처다.
 */
fun <ITEM:Any, OTHER:Any> ListF<ITEM>.flatMap(block:(ITEM)-> ListF<OTHER>): ListF<OTHER>
= foldRight(ListF.empty()) {it, acc ->
    val other= block(it)
    if(other==Nil) acc else other.foldRight(acc, ::Cons)
}

/**
 * 연습문제 3.20
 * flatMap을 사용해 filter를 구현하라.
 */
fun <ITEM:Any> ListF<ITEM>.flatMapFilter(block:(ITEM)-> Boolean): ListF<ITEM>
= flatMap { if(block(it)) Cons(it, Nil) else Nil }

/**
 * 연습문제 3.21
 * 두 리스트를 받아서 서로 같은 위치(인덱스)에 있는 원소들을 더한 값으로 이뤄진 새 리스트를 돌려주는 함수를 작성하라.
 * 예를 들어 List(1,2,3)과 List(4,5,6)은 List(5,7,9)가 된다.
 */
fun ListF<Int>.zip(list:ListF<Int>): ListF<Int>
= when(this) {
    is Cons-> when(list) {
        is Cons-> Cons(head+list.head, tail.zip(list.tail))
        is Nil-> Nil
    }
    is Nil-> Nil
}

/**
 * 연습문제 3.22
 * 연습문제 3.21에서 작성한 함수를 일반화해 정수 타입(리스트의 원소 타입)이나 덧셈(대응하는 원소에 작용하는 연산)에
 * 한정되지 않고 다양한 처리가 가능하게 하라. 이 일반화한 함수에는 zipWith라는 이름을 붙여라.
 */
fun <ITEM:Any> ListF<ITEM>.zipWith(list: ListF<ITEM>, block:(ITEM, ITEM)-> ITEM): ListF<ITEM>
= when(this) {
    is Cons-> when(list) {
        is Cons-> Cons(block(head, list.head), tail.zipWith(list.tail, block))
        is Nil-> Nil
    }
    is Nil-> Nil
}

/**
 * 연습문제 3.23
 * 어떤 List가 다른 List를 부분열로 포함하는지 검사하는 hasSubsequence를 구현하라.
 * 예를 들어 List(1,2,3,4)의 부분 시퀀스는 List(1,2), List(2,3), List(4)등이 있다.
 * 효율적인 동시에 간결한 순수 함수형 해법을 찾아내기는 어려울 수도 있다.
 * 그래도 괜찮다. 가장 자연스럽게 떠오르는 아이디어로 함수를 구현하라.
 * 5장에서 이 함수 구현을 다시 살펴보면, 바라건대 이를 더 개선할 수 있을 것이다.
 * 힌트: 코틀린에서 두 값 x와 y가 동등한지 비교하려면 x == y를 쓴다.
 */
tailrec fun <ITEM:Any> ListF<ITEM>.hasSubsequence(sub: ListF<ITEM>): Boolean
= when(this) {
    is Cons-> if(startsWith(this, sub)) true else tail.hasSubsequence(sub)
    is Nil-> false
}
tailrec fun <ITEM:Any> startsWith(target:ListF<ITEM>, sub:ListF<ITEM>): Boolean
= when(target) {
    is Cons-> when(sub) {
        is Cons-> if(target.head == sub.head) startsWith(target.tail, sub.tail) else false
        is Nil-> true
    }
    is Nil-> sub == Nil
}


package khs

import khs.TreeF.Leaf
import khs.TreeF.Branch

sealed class TreeF<out ITEM:Any> {
    companion object {
        fun <ITEM:Any> of(v:ITEM)= Leaf(v)
        fun <ITEM:Any> of(l:TreeF<ITEM>, r:TreeF<ITEM>)= Branch(l,r)
    }
    data class Leaf<ITEM:Any>(val value:ITEM): TreeF<ITEM>()
    data class Branch<ITEM:Any>(val left:TreeF<ITEM>, val right:TreeF<ITEM>): TreeF<ITEM>()
}

/**
 * 연습문제 3.24
 * 트리 안에 들어 있는 노드(잎과 가지를 모두 포함)의 개수를 반환하는 size 함수를 작성하라.
 */
fun <ITEM:Any> TreeF<ITEM>.size(): Int
= when(this) {
    is Leaf-> 1
    is Branch-> 1+ left.size()+ right.size()
}

/**
 * 연습문제 3.25
 * Tree<Int>에서 가장 큰 원소를 돌려주는 maximum 함수를 작성하라.
 * 힌트: 코틀린은 두 값의 최댓값을 결정해주는 maxOf라는 내장 함수를 제공한다.
 * 예를 들어 x와 y중 최댓값은 maxOf(x, y)이다.
 */
fun TreeF<Int>.maximum(): Int
= when(this) {
    is Leaf-> value
    is Branch-> maxOf(left.maximum(), right.maximum())
}

/**
 * 연습문제 3.26
 * 트리 뿌리에서 각 잎까지의 경로 중 가장 길이가 긴(간선의 수가 경로의 길이임) 값을 돌려주는 depth함수를 작성하라.
 */
fun <ITEM:Any> TreeF<ITEM>.depth(): Int
= when(this) {
    is Leaf-> 0
    is Branch-> 1+ maxOf(left.depth(), right.depth())
}

/**
 * 연습문제 3.27
 * List에 정의했던 map과 대응하는 map 함수를 정의하라.
 * 이 map은 트리의 모든 원소를 주어진 함수를 사용해 변환한 새 함수를 반환한다.
 */
fun <ITEM:Any, OTHER:Any> TreeF<ITEM>.map(block:(ITEM)-> OTHER): TreeF<OTHER>
= when(this) {
    is Leaf-> Leaf(block(value))
    is Branch-> Branch(left.map(block), right.map(block))
}

/**
 * 연습문제 3.28
 * Tree에서 size, maximum, depth, map을 일반화해 이 함수들의 유사한 점을 추상화한 새로운 fold 함수를 작성하라.
 * 그리고 더 일반적인 이 fold 함수를 사용해 size, maximum, depth, map을 재구현하라.
 * List의 오른쪽/왼쪽 폴드와 여기서 정의한 fold 사이에 유사점을 찾아낼 수 있는가
 */
fun <ITEM:Any, OTHER:Any> TreeF<ITEM>.fold(leaf:(ITEM)->OTHER, branch:(OTHER,OTHER)->OTHER): OTHER
= when(this) {
    is Leaf-> leaf(value)
    is Branch-> branch(left.fold(leaf, branch), right.fold(leaf, branch))
}
fun <ITEM:Any> TreeF<ITEM>.sizeF(): Int = fold({1}) {l, r -> 1+ l+ r}
fun TreeF<Int>.maximumF(): Int = fold({it}) {l, r -> maxOf(l, r)}
fun TreeF<Int>.depthF(): Int = fold({0}) {l, r -> 1+ maxOf(l, r)}
fun <ITEM:Any, OTHER:Any> TreeF<ITEM>.mapF(block:(ITEM)->OTHER): TreeF<OTHER>
= fold({Leaf(block(it))}) {l:TreeF<OTHER>, r:TreeF<OTHER> -> Branch(l, r)}


// ========== 3장 테스트 ==========
package khs

import kotlin.test.Test
import kotlin.test.assertEquals

class Chapter3Test {
    @Test
    fun listTest() {
        val intList = ListF.of(1, 2, 3, 4)
        val doubleList = ListF.of(1.0,2.0,3.0,4.0)

        println("=====> list: $intList")
        assertEquals(intList.tail(), ListF.of(2,3,4))
        assertEquals(intList.setHead(10), ListF.of(10,2,3,4))
        assertEquals(intList.drop(2), ListF.of(3,4))
        assertEquals(intList.dropWhile { it<=2 }, ListF.of(3,4))
        assertEquals(intList.init(), ListF.of(1,2,3))
        assertEquals(intList.sum(), 10)
        assertEquals(doubleList.product(), 24.0)
        assertEquals(intList.foldRight(0) { it, acc -> it + acc}, 10)
        assertEquals(intList.sumFoldRight(), 10)
        assertEquals(doubleList.productFoldRight(), 24.0)
        assertEquals(intList.length(), 4)
        assertEquals(intList.foldLeft(0) { acc, it -> it + acc}, 10)
        assertEquals(intList.sumFoldLeft(), 10)
        assertEquals(doubleList.productFoldLeft(), 24.0)
        assertEquals(intList.lengthFoldLeft(), 4)
        assertEquals(intList.reverse(), ListF.of(4,3,2,1))
        assertEquals(intList.foldRightL(0) { it, acc -> it + acc}, 10)
        assertEquals(intList.foldLeftR(0) {acc, it -> it + acc}, 10)
        assertEquals(intList.append(ListF.of(10,20)), ListF.of(1,2,3,4,10,20))
        assertEquals(ListF.of(ListF.of(1,2), ListF.of(3,4)).flatten(), ListF.of(1,2,3,4))
        assertEquals(intList.intPlusOne(), ListF.of(2,3,4,5))
        assertEquals(doubleList.doubleToString(), ListF.of("1.0","2.0","3.0","4.0"))
        assertEquals(intList.map {"$it"}, ListF.of("1","2","3","4"))
        assertEquals(intList.filter {it%2 == 1}, ListF.of(1,3))
        assertEquals(intList.flatMap {if(it%2 == 1) ListF.of(it) else ListF.empty()}, ListF.of(1,3))
        assertEquals(intList.flatMapFilter {it%2 == 1}, ListF.of(1,3))
        assertEquals(intList.zip(ListF.of(1,2,3,4)), ListF.of(2,4,6,8))
        assertEquals(intList.zipWith(ListF.of(1,2,3,4)) {a, b -> a+b}, ListF.of(2,4,6,8))
        assertEquals(intList.hasSubsequence(ListF.of(1,2)), true)
    }

    @Test
    fun treeTest() {
        val tree= TreeF.of(TreeF.of(1), TreeF.of(TreeF.of(TreeF.of(2), TreeF.of(22)), TreeF.of(3)))
        println("=====> tree: $tree")

        assertEquals(tree.size(), 7)
        assertEquals(tree.maximum(), 22)
        assertEquals(tree.depth(), 3)
        assertEquals(tree.map{"$it"}, TreeF.of(TreeF.of("1"), TreeF.of(TreeF.of(TreeF.of("2"), TreeF.of("22")), TreeF.of("3"))))
        assertEquals(tree.fold({"$it"}) {l, r -> l+r}, "12223")
        assertEquals(tree.sizeF(), 7)
        assertEquals(tree.maximumF(), 22)
        assertEquals(tree.depthF(), 3)
        assertEquals(tree.mapF{"$it"}, TreeF.of(TreeF.of("1"), TreeF.of(TreeF.of(TreeF.of("2"), TreeF.of("22")), TreeF.of("3"))))
    }
}

// ========== 연습문제4 ==========
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

// ===== 연습문제5 =====
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

// ===== 연습문제6 =====
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
